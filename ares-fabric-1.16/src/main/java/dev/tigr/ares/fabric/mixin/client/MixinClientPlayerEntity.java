package dev.tigr.ares.fabric.mixin.client;

import com.mojang.authlib.GameProfile;
import dev.tigr.ares.Wrapper;
import dev.tigr.ares.core.Ares;
import dev.tigr.ares.core.event.render.PortalChatEvent;
import dev.tigr.ares.core.feature.module.Module;
import dev.tigr.ares.fabric.event.movement.*;
import dev.tigr.ares.fabric.impl.modules.player.Freecam;
import dev.tigr.ares.fabric.mixin.accessors.ClientPlayerEntityAccessor;
import dev.tigr.ares.fabric.mixin.accessors.EntityAccessor;
import dev.tigr.ares.fabric.utils.Reimplementations;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.MovementType;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * @author Tigermouthbear
 */
@Mixin(ClientPlayerEntity.class)
public class MixinClientPlayerEntity extends AbstractClientPlayerEntity implements Wrapper {
    ClientPlayerEntity clientPlayerEntity = (ClientPlayerEntity) (Object) this;

    public MixinClientPlayerEntity(ClientWorld world, GameProfile profile) {
        super(world, profile);
    }

    @Inject(method = "tickMovement", at = @At("HEAD"))
    public void onMotion(CallbackInfo ci) {
        Module.motion();
    }

    @Inject(method = "pushOutOfBlocks", at = @At("HEAD"), cancellable = true)
    public void pushOutOfBlocks(double d, double d1, CallbackInfo ci) {
        if(Ares.EVENT_MANAGER.post(new EntityClipEvent(clientPlayerEntity)).isCancelled()) ci.cancel();
    }

    @Inject(method = "pushOutOfBlocks", at = @At("HEAD"), cancellable = true)
    public void noPushOutOfBlocks(double var1, double var2, CallbackInfo ci) {
        BlockPushEvent blockPushEvent = Ares.EVENT_MANAGER.post((new BlockPushEvent(var1, var2)));
        if (Ares.EVENT_MANAGER.post(new BlockPushEvent(var1, var2)).isCancelled()) ci.cancel();
    }

    @Inject(method = "tickMovement", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerEntity;isUsingItem()Z", ordinal = 0))
    public void slowdownPlayer(CallbackInfo ci) {
        if(clientPlayerEntity.isUsingItem() && Ares.EVENT_MANAGER.post(new SlowDownEvent()).isCancelled()) {
            clientPlayerEntity.input.movementSideways /= 0.2F;
            clientPlayerEntity.input.movementForward /= 0.2F;
        }
    }

    @Redirect(method = "updateNausea", at = @At(value = "FIELD", target = "Lnet/minecraft/client/network/ClientPlayerEntity;inNetherPortal:Z", ordinal = 0))
    public boolean portalChat(ClientPlayerEntity clientPlayerEntity) {
        return ((EntityAccessor) clientPlayerEntity).isInNetherPortal() && !Ares.EVENT_MANAGER.post(new PortalChatEvent()).isCancelled();
    }

    @Redirect(method = "move", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/AbstractClientPlayerEntity;move(Lnet/minecraft/entity/MovementType;Lnet/minecraft/util/math/Vec3d;)V"))
    public void movePlayer(AbstractClientPlayerEntity abstractClientPlayerEntity, MovementType type, Vec3d movement) {
        MovePlayerEvent event = Ares.EVENT_MANAGER.post(new MovePlayerEvent(type, movement.x, movement.y, movement.z));
        if(!event.isCancelled()) {
            if(event.getShouldDo()) super.move(type, new Vec3d(event.getX(), event.getY(), event.getZ()));
            else super.move(type, movement);
        }
    }

    /* Rotations Start */
    @Inject(method = "sendMovementPackets", at = @At("HEAD"), cancellable = true)
    public void onSendMovementPackets(CallbackInfo ci) {
        SendMovementPacketsEvent event = new SendMovementPacketsEvent.Pre(getPos(), onGround);
        if(Ares.EVENT_MANAGER.post(event).isCancelled()) {
            if(!event.isModifying()) {
                ci.cancel();
                return;
            }
            ci.cancel();

            //Modified packets
            boolean bl = MC.player.isSprinting();

            if(bl != ((ClientPlayerEntityAccessor) MC.player).lastSneaking()) {
                ClientCommandC2SPacket.Mode mode = bl ? ClientCommandC2SPacket.Mode.START_SPRINTING : ClientCommandC2SPacket.Mode.STOP_SPRINTING;
                MC.player.networkHandler.sendPacket(new ClientCommandC2SPacket(MC.player, mode));
                ((ClientPlayerEntityAccessor) MC.player).lastSneaking(bl);
            }

            boolean bl2 = MC.player.isSneaking();

            if(bl2 != (((ClientPlayerEntityAccessor) MC.player).lastSneaking())) {
                ClientCommandC2SPacket.Mode mode2 = bl2 ? ClientCommandC2SPacket.Mode.PRESS_SHIFT_KEY : ClientCommandC2SPacket.Mode.RELEASE_SHIFT_KEY;
                MC.player.networkHandler.sendPacket(new ClientCommandC2SPacket(MC.player, mode2));
                ((ClientPlayerEntityAccessor) MC.player).lastSneaking(bl2);
            }

            if(((ClientPlayerEntityAccessor) MC.player).client().getCameraEntity() == MC.player) {
                double d = MC.player.getX() - ((ClientPlayerEntityAccessor) MC.player).lastX();
                double e = event.getPos().y - ((ClientPlayerEntityAccessor) MC.player).lastBaseY();
                double f = MC.player.getZ() - ((ClientPlayerEntityAccessor) MC.player).lastZ();
                double g = (event.getYaw() - ((ClientPlayerEntityAccessor) MC.player).lastYaw());
                double h = (event.getPitch() - ((ClientPlayerEntityAccessor) MC.player).lastPitch());

                ((ClientPlayerEntityAccessor) MC.player).ticksSinceLastPositionPacketSent(((ClientPlayerEntityAccessor) MC.player).ticksSinceLastPositionPacketSent() +1);

                boolean bl3 = d * d + e * e + f * f > 9.0E-4D || ((ClientPlayerEntityAccessor) MC.player).ticksSinceLastPositionPacketSent() >= 20;
                boolean bl4 = g != 0.0D || h != 0.0D;

                if(Freecam.INSTANCE.getEnabled()) bl3 = false;

                if(MC.player.hasVehicle()) {
                    Vec3d vec3d = MC.player.getVelocity();
                    if(!Freecam.INSTANCE.getEnabled()) MC.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.Both(vec3d.x, -999.0D, vec3d.z, event.getYaw(), event.getPitch(), MC.player.isOnGround()));
                    bl3 = false;
                }

                else if(bl3 && bl4) MC.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.Both(MC.player.getX(), event.getPos().y, MC.player.getZ(), event.getYaw(), event.getPitch(), MC.player.isOnGround()));

                else if(bl3) MC.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.PositionOnly(MC.player.getX(), event.getPos().y, MC.player.getZ(), MC.player.isOnGround()));

                else if(bl4) {
                    if(Freecam.INSTANCE.getEnabled() && !event.isModifying())
                        MC.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.LookOnly(Freecam.INSTANCE.clone.yaw, Freecam.INSTANCE.clone.pitch, MC.player.isOnGround()));

                    else MC.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.LookOnly(event.getYaw(), event.getPitch(), MC.player.isOnGround()));
                }

                else if(((ClientPlayerEntityAccessor) MC.player).lastOnGround() != MC.player.isOnGround()) MC.player.networkHandler.sendPacket(new PlayerMoveC2SPacket(MC.player.isOnGround()));

                if(bl3) {
                    ((ClientPlayerEntityAccessor) MC.player).lastX(MC.player.getX());
                    ((ClientPlayerEntityAccessor) MC.player).lastBaseY(event.getPos().y);
                    ((ClientPlayerEntityAccessor) MC.player).lastZ(MC.player.getZ());
                    ((ClientPlayerEntityAccessor) MC.player).ticksSinceLastPositionPacketSent(0);
                }

                if(bl4) {
                    ((ClientPlayerEntityAccessor) MC.player).lastYaw(event.getYaw());
                    ((ClientPlayerEntityAccessor) MC.player).lastPitch(event.getPitch());
                }

                ((ClientPlayerEntityAccessor) MC.player).lastOnGround(MC.player.isOnGround());
                ((ClientPlayerEntityAccessor) MC.player).autoJumpEnabled(MC.options.autoJump);
            }

            Ares.EVENT_MANAGER.post(new SendMovementPacketsEvent.Post(event.getYaw(), event.getPitch()));
        }
    }
    /* Rotations End */

    @Override
    public void jump() {
        PlayerJumpEvent event = new PlayerJumpEvent();
        Ares.EVENT_MANAGER.post(event);
        if(!event.isCancelled()) super.jump();
    }
}
