package dev.tigr.ares.fabric.impl.modules.player;

import dev.tigr.ares.core.feature.module.Category;
import dev.tigr.ares.core.feature.module.Module;
import dev.tigr.ares.core.setting.Setting;
import dev.tigr.ares.core.setting.settings.BooleanSetting;
import dev.tigr.ares.core.setting.settings.numerical.IntegerSetting;
import dev.tigr.ares.core.util.Timer;
import dev.tigr.ares.fabric.event.movement.SendMovementPacketsEvent;
import dev.tigr.ares.fabric.event.render.PlayerModelRenderEvent;
import dev.tigr.ares.fabric.utils.WorldUtils;
import dev.tigr.simpleevents.listener.EventHandler;
import dev.tigr.simpleevents.listener.EventListener;
import net.minecraft.block.FluidBlock;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.util.math.*;

/**
 * @author Makrennel 10/11/21
 */
@Module.Info(name = "RotationManager", description = "Handles all packet rotations (Disabling turns off rotations entirely!!!)", category = Category.PLAYER, enabled = true, visible = false)
public class RotationManager extends Module {
    public static RotationManager ROTATIONS;

    private final Setting<Integer> resetDelay = register(new IntegerSetting("Reset Delay", 20, 0, 40));
    private final Setting<Boolean> renderBodyYaw = register(new BooleanSetting("Render Body Yaw", true));

    public RotationManager() {
        ROTATIONS = this;
    }

    Timer rotationDelay = new Timer();
    Vec2f currentRotation = null;
    int currentPriority = -1;
    int key = -1;
    boolean completedAction;

    public boolean setCurrentRotation(float yaw, float pitch, int key, int priority, boolean instant, boolean instantBypassCurrent) {
        return setCurrentRotation(new Vec2f(yaw, pitch), priority, key, instant, instantBypassCurrent);
    }
    public boolean setCurrentRotation(Vec2f rotation, int key, int priority, boolean instant, boolean instantBypassCurrent) {
        if(currentRotation == null || currentPriority <= priority || this.key == key || this.key == -1 || completedAction) {
            //Rotate instantly if specified, but only if the rotation does not match (to try and reduce rubberbanding)
            if(instant && currentRotation != rotation) MC.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.LookAndOnGround(rotation.x, rotation.y, MC.player.isOnGround()));

            currentRotation = rotation;
            currentPriority = priority;
            this.key = key;
            rotationDelay.reset();
            return true;
        }

        //If bypass current flagged, use instant rotation anyways and return true for placement (might not actually be useful)
        if(instant && instantBypassCurrent && currentRotation != rotation) {
            MC.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.LookAndOnGround(rotation.x, rotation.y, MC.player.isOnGround()));
            return true;
        }
        return false;
    }

    public void setCompletedAction(int key, boolean completedAction) {
        //If action is complete another key with lower priority can take over and set a new rotation without having to wait for reset delay to pass
        if(this.key == key) {
            this.completedAction = completedAction;

            //Can use this to reset rotation reset delay
            if(!completedAction) rotationDelay.reset();
        }
    }

    public Vec2f getCurrentRotation() {
        return currentRotation;
    }

    public boolean isKeyCurrent(int key) {
        return this.key == key;
    }

    public boolean isCompletedAction() {
        return completedAction;
    }

    public int getCurrentPriority() {
        return currentPriority;
    }

    @EventHandler
    private final EventListener<SendMovementPacketsEvent.Pre> onMovementPacketSent = new EventListener<>(event -> {
        if(currentRotation == null) {
            rotationDelay.reset();
            return;
        }

        //If reset delay has passed reset everything
        if(rotationDelay.passedTicks(resetDelay.getValue())) {
            currentRotation = null;
            currentPriority = -1;
            key = -1;
            completedAction = false;
            rotationDelay.reset();
            return;
        }

        event.setRotation(currentRotation);
        event.setCancelled(true);

        //Apply adjustments to Freecam model if necessary
        if(Freecam.INSTANCE.getEnabled()) {
            Freecam.INSTANCE.clone.headYaw = currentRotation.x;
            Freecam.INSTANCE.clone.bodyYaw = currentRotation.x;
            Freecam.INSTANCE.clone.setYaw(currentRotation.x);
            Freecam.INSTANCE.clone.setPitch(currentRotation.y);
        }
    });

    @EventHandler
    public final EventListener<PlayerModelRenderEvent> onPlayerModelRender = new EventListener<>(event -> {
        if(event.getLivingEntity() == MC.player && currentRotation != null) event.setCancelled(true);
        else return;
        event.setHeadPitch(currentRotation.y);
        event.setHeadYaw(currentRotation.x);
        if(renderBodyYaw.getValue()) event.setBodyYaw(currentRotation.x);
    });

    //Method for getting the angle to a random neighbor's side (may be useful for pre-rotating as this is the same one from WorldUtils.placeBlock())
    public static Vec2f getPlaceSideAngle(BlockPos pos) {
        Vec3d eyesPos = new Vec3d(WorldUtils.getPlayer().getX(),
                WorldUtils.getPlayer().getY() + WorldUtils.getPlayer().getEyeHeight(WorldUtils.getPlayer().getPose()),
                WorldUtils.getPlayer().getZ());

        Vec3d hitVec = null;
        BlockPos neighbor;
        Direction side2;

        for(Direction side: Direction.values()) {
            neighbor = pos.offset(side);
            side2 = side.getOpposite();

            // check if neighbor can be right clicked aka it isnt air
            if (MC.world.getBlockState(neighbor).isAir() || MC.world.getBlockState(neighbor).getBlock() instanceof FluidBlock) {
                neighbor = null;
                side2 = null;
                continue;
            }

            hitVec = new Vec3d(neighbor.getX(), neighbor.getY(), neighbor.getZ()).add(0.5, 0.5, 0.5).add(new Vec3d(side2.getUnitVector()).multiply(0.5));
            break;
        }

        if(hitVec == null) hitVec = new Vec3d(pos.getX() +0.5, pos.getY() +0.5, pos.getZ() +0.5);

        double diffX = hitVec.x - eyesPos.x;
        double diffY = hitVec.y - eyesPos.y;
        double diffZ = hitVec.z - eyesPos.z;

        double diffXZ = Math.sqrt(diffX * diffX + diffZ * diffZ);

        float yaw = (float) Math.toDegrees(Math.atan2(diffZ, diffX)) - 90F;
        float pitch = (float) -Math.toDegrees(Math.atan2(diffY, diffXZ));

        return new Vec2f(WorldUtils.getPlayer().getYaw() + MathHelper.wrapDegrees(yaw - WorldUtils.getPlayer().getYaw()), WorldUtils.getPlayer().getPitch() + MathHelper.wrapDegrees(pitch - WorldUtils.getPlayer().getPitch()));
    }
}
