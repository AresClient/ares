package dev.tigr.ares.forge.impl.modules.movement;

import dev.tigr.ares.core.feature.module.Category;
import dev.tigr.ares.core.feature.module.Module;
import dev.tigr.ares.core.setting.Setting;
import dev.tigr.ares.core.setting.settings.BooleanSetting;
import dev.tigr.ares.core.setting.settings.numerical.DoubleSetting;
import dev.tigr.ares.core.setting.settings.numerical.FloatSetting;
import dev.tigr.ares.core.setting.settings.numerical.IntegerSetting;
import dev.tigr.ares.forge.event.events.client.NetworkExceptionEvent;
import dev.tigr.ares.forge.event.events.movement.BoatMoveEvent;
import dev.tigr.ares.forge.event.events.player.PacketEvent;
import dev.tigr.simpleevents.listener.EventHandler;
import dev.tigr.simpleevents.listener.EventListener;
import io.netty.buffer.Unpooled;
import net.minecraft.entity.item.EntityBoat;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.client.*;
import net.minecraft.network.play.server.SPacketMoveVehicle;
import net.minecraft.network.play.server.SPacketPlayerPosLook;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.Vec3d;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Tigermouthbear
 */
@Module.Info(name = "BoatFly", description = "", category = Category.MOVEMENT)
public class BoatFly extends Module {
    public static BoatFly INSTANCE;

    private final Setting<Double> speed = register(new DoubleSetting("Speed", 0.9D, 0.01D, 12.0D));
    private final Setting<Double> fallSpeed = register(new DoubleSetting("Fall Speed", 0, 0, 2));
    private final Setting<Float> opacity = register(new FloatSetting("Opacity", 1, 0, 1));
    private final Setting<Boolean> bypass = register(new BooleanSetting("Bypass", false));
    private final Setting<Integer> interval = register(new IntegerSetting("Bypass Interval", 2, 1, 10)).setVisibility(bypass::getValue);
    private final Setting<Boolean> phase = register(new BooleanSetting("Phase", false));

    private final List<CPacketVehicleMove> packets = new ArrayList<>();
    private EntityBoat boat = null;
    private int tpId = 0;

    public BoatFly() {
        INSTANCE = this;
    }

    @Override
    public void onDisable() {
        if(boat != null) boat.noClip = false;
        boat = null;
        packets.clear();
    }

    @EventHandler
    public EventListener<BoatMoveEvent> boatMoveEvent = new EventListener<>(event -> {
        if(boat == null || boat != event.getBoat()) boat = event.getBoat();

        event.y = 0;

        boat.rotationYaw = MC.player.rotationYaw;

        // set phase
        if(phase.getValue()) {
            boat.noClip = true;
            boat.entityCollisionReduction = 1;
        } else boat.noClip = false;

        if(!MC.gameSettings.keyBindForward.isKeyDown() &&
                !MC.gameSettings.keyBindBack.isKeyDown() &&
                !MC.gameSettings.keyBindLeft.isKeyDown() &&
                !MC.gameSettings.keyBindRight.isKeyDown()) event.x = event.z = 0;
        else {
            float yaw = MC.player.rotationYaw;
            float forward = 1;

            if(MC.player.moveForward < 0) {
                yaw += 180;
                forward = -0.5f;
            } else if(MC.player.moveForward > 0) forward = 0.5f;

            if(MC.player.moveStrafing > 0) yaw -= 90 * forward;
            if(MC.player.moveStrafing < 0) yaw += 90 * forward;

            yaw = (float) Math.toRadians(yaw);

            event.x = -Math.sin(yaw) * speed.getValue();
            event.z = Math.cos(yaw) * speed.getValue();
        }

        if(MC.gameSettings.keyBindSprint.isKeyDown()) event.y = -speed.getValue();
        else if(MC.gameSettings.keyBindJump.isKeyDown()) event.y += speed.getValue();
        else event.y = -fallSpeed.getValue();
    });

    @EventHandler
    public EventListener<PacketEvent.Receive> packetReceiveEvent = new EventListener<>(event -> {
        if(bypass.getValue() && MC.player != null && MC.player.isRiding()) {
            if(event.getPacket() instanceof SPacketPlayerPosLook) {
                tpId = ((SPacketPlayerPosLook) event.getPacket()).getTeleportId();

                event.setCancelled(true);
            }
            if(event.getPacket() instanceof SPacketMoveVehicle) event.setCancelled(true);
        }
    });

    private Vec3d getRidingPosition() {
        float f = 0.0F;
        float f1 = (float)((boat.isDead ? 0.009999999776482582D : boat.getMountedYOffset()) + MC.player.getYOffset());

        if(boat.getPassengers().size() > 1) {
            if(boat.getPassengers().indexOf(MC.player) == 0) f = 0.2F;
            else f = -0.6F;
        }

        Vec3d vector = (new Vec3d(f, 0.0D, 0.0D)).rotateYaw(-boat.rotationYaw * 0.017453292F - ((float)Math.PI / 2F));
        return new Vec3d(boat.posX + vector.x, boat.posY + (double)f1, boat.posZ + vector.z);
    }

    @EventHandler
    public EventListener<PacketEvent.Sent> packetSentEvent = new EventListener<>(event -> {
        if(bypass.getValue() && MC.player != null && MC.player.isRiding() && boat != null) {
            if(event.getPacket() instanceof CPacketVehicleMove && MC.player.ticksExisted % interval.getValue() == 0) {
                if(packets.contains(event.getPacket())) packets.remove(event.getPacket());
                else {
                    // move boat to previous location
                    try {
                        event.getPacket().readPacketData(createPacketData(boat.prevPosX, boat.prevPosY, boat.prevPosZ, boat.prevRotationYaw, boat.prevRotationPitch));
                    } catch(IOException e) {
                        UTILS.printMessage("Error while using boatfly, disabling...");
                        setEnabled(false);
                    }

                    // cause dismount then remount
                    MC.player.connection.sendPacket(new CPacketUseEntity(boat, EnumHand.OFF_HAND));

                    // move boat to current location
                    MC.player.connection.sendPacket(add(new CPacketVehicleMove(boat)));

                    // send player position
                    Vec3d pos = getRidingPosition();
                    MC.player.connection.sendPacket(new CPacketPlayer.Position(pos.x, pos.y, pos.z, MC.player.onGround));

                    // confirm with predicted tpId
                    ++tpId;
                    MC.player.connection.sendPacket(new CPacketConfirmTeleport(tpId));
                }
            }

            if(event.getPacket() instanceof CPacketInput || event.getPacket() instanceof CPacketPlayer.Rotation || event.getPacket() instanceof CPacketSteerBoat) event.setCancelled(true);
        }
    });

    @EventHandler
    public EventListener<NetworkExceptionEvent> networkExceptionEvent = new EventListener<>(event -> event.setCancelled(true));

    private CPacketVehicleMove createPacket(double x, double y, double z, float yaw, float pitch) {
        CPacketVehicleMove packet = new CPacketVehicleMove();

        try {
            packet.readPacketData(createPacketData(x, y, z, yaw, pitch));
        } catch(IOException e) {
            UTILS.printMessage("Error while using boatfly, disabling...");
            setEnabled(false);
        }

        return packet;
    }

    private PacketBuffer createPacketData(double x, double y, double z, float yaw, float pitch) {
        PacketBuffer buffer = new PacketBuffer(Unpooled.buffer());

        buffer.writeDouble(x);
        buffer.writeDouble(y);
        buffer.writeDouble(z);
        buffer.writeFloat(yaw);
        buffer.writeFloat(pitch);

        return buffer;
    }

    private <T extends CPacketVehicleMove> T add(T packet) {
        packets.add(packet);
        return packet;
    }

    public float getOpacity() {
        return opacity.getValue();
    }
}
