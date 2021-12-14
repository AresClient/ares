package dev.tigr.ares.forge.mixin.client;

import dev.tigr.ares.Wrapper;
import dev.tigr.ares.core.Ares;
import dev.tigr.ares.core.event.movement.PlayerJumpEvent;
import dev.tigr.ares.forge.event.events.movement.ElytraMoveEvent;
import dev.tigr.ares.forge.event.events.movement.SmoothElytraEvent;
import dev.tigr.ares.forge.event.events.movement.WaterMoveEvent;
import dev.tigr.ares.forge.event.events.player.StatusEffectEvent;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.MoverType;
import net.minecraft.potion.Potion;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * @author Tigermouthbear
 */
@Mixin(EntityLivingBase.class)
public abstract class MixinEntityLivingBase implements Wrapper {
    private final EntityLivingBase entity = (EntityLivingBase) (Object) this;

    @Redirect(method = "travel", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/EntityLivingBase;move(Lnet/minecraft/entity/MoverType;DDD)V", ordinal = 0))
    public void elytraMove(EntityLivingBase entityLivingBase, MoverType type, double x, double y, double z) {
        ElytraMoveEvent elytraMoveEvent = Ares.EVENT_MANAGER.post(new ElytraMoveEvent(x, y, z));
        entityLivingBase.move(type, elytraMoveEvent.x, elytraMoveEvent.y, elytraMoveEvent.z);
    }

    @Redirect(method = "travel", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/EntityLivingBase;move(Lnet/minecraft/entity/MoverType;DDD)V", ordinal = 3))
    public void waterMove(EntityLivingBase entityLivingBase, MoverType type, double x, double y, double z) {
        WaterMoveEvent waterMoveEvent = Ares.EVENT_MANAGER.post(new WaterMoveEvent(x, y, z));
        entityLivingBase.move(type, waterMoveEvent.getX(), waterMoveEvent.getY(), waterMoveEvent.getZ());
    }

    @Redirect(method = "travel", at = @At(value = "FIELD", target = "Lnet/minecraft/world/World;isRemote:Z", ordinal = 1))
    public boolean isWorldRemoteWrapper(World world) {
        return Ares.EVENT_MANAGER.post(new SmoothElytraEvent(world.isRemote)).isWorldRemote;
    }

    @Inject(method = "isPotionActive", at = @At("RETURN"), cancellable = true)
    public void isPotionActive(Potion potionIn, CallbackInfoReturnable<Boolean> cir) {
        if(Ares.EVENT_MANAGER.post(new StatusEffectEvent(entity, potionIn)).isCancelled()) cir.setReturnValue(false);
    }

    @Inject(method = "jump", at = @At("HEAD"), cancellable = true)
    public void onJump(CallbackInfo ci) {
        if(entity != MC.player) return;
        if(Ares.EVENT_MANAGER.post(new PlayerJumpEvent()).isCancelled()) ci.cancel();
    }
}
