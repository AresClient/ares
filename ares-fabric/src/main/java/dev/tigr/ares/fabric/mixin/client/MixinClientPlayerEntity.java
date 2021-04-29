package dev.tigr.ares.fabric.mixin.client;

import com.mojang.authlib.GameProfile;
import dev.tigr.ares.core.Ares;
import dev.tigr.ares.core.event.render.PortalChatEvent;
import dev.tigr.ares.core.feature.module.Module;
import dev.tigr.ares.core.util.global.ReflectionHelper;
import dev.tigr.ares.fabric.event.movement.*;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.MovementType;
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
public class MixinClientPlayerEntity extends AbstractClientPlayerEntity {
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
        return (boolean) ReflectionHelper.getPrivateValue(Entity.class, clientPlayerEntity, "inNetherPortal", "field_5963") && !Ares.EVENT_MANAGER.post(new PortalChatEvent()).isCancelled();
    }

    @Redirect(method = "move", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/AbstractClientPlayerEntity;move(Lnet/minecraft/entity/MovementType;Lnet/minecraft/util/math/Vec3d;)V"))
    public void movePlayer(AbstractClientPlayerEntity abstractClientPlayerEntity, MovementType type, Vec3d movement) {
        MovePlayerEvent event = Ares.EVENT_MANAGER.post(new MovePlayerEvent(type, movement.x, movement.y, movement.z));
        if(!event.isCancelled()) {
            if(event.getShouldDo()) super.move(type, new Vec3d(event.getX(), event.getY(), event.getZ()));
            else super.move(type, movement);
        }
    }

    @Override
    public void jump() {
        PlayerJumpEvent event = new PlayerJumpEvent();
        Ares.EVENT_MANAGER.post(event);
        if(!event.isCancelled()) super.jump();
    }
}
