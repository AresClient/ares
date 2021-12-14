package dev.tigr.ares.fabric.mixin.client;

import dev.tigr.ares.Wrapper;
import dev.tigr.ares.core.Ares;
import dev.tigr.ares.core.event.movement.PlayerJumpEvent;
import dev.tigr.ares.fabric.event.client.LivingDeathEvent;
import dev.tigr.ares.fabric.event.client.UpdateLivingEntityEvent;
import dev.tigr.ares.fabric.event.movement.ElytraMoveEvent;
import dev.tigr.ares.fabric.event.player.StatusEffectEvent;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MovementType;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * @author Tigermouthbear 9/26/20
 */
@Mixin(LivingEntity.class)
public class MixinLivingEntity implements Wrapper {
    private final LivingEntity entity = ((LivingEntity) (Object) this);

    @Inject(method = "onDeath", at = @At("HEAD"))
    public void death(DamageSource damageSource, CallbackInfo ci) {
        Ares.EVENT_MANAGER.post(new LivingDeathEvent(entity, damageSource));
    }

    @Redirect(method = "travel", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;move(Lnet/minecraft/entity/MovementType;Lnet/minecraft/util/math/Vec3d;)V", ordinal = 2))
    public void elytraMove(LivingEntity livingEntity, MovementType movementType, Vec3d vec3d) {
        ElytraMoveEvent elytraMoveEvent = Ares.EVENT_MANAGER.post(new ElytraMoveEvent(vec3d.x, vec3d.y, vec3d.z));
        livingEntity.move(movementType, new Vec3d(elytraMoveEvent.x, elytraMoveEvent.y, elytraMoveEvent.z));
    }

    @Inject(method = "baseTick", at = @At("HEAD"))
    public void baseTickHead(CallbackInfo ci) {
        Ares.EVENT_MANAGER.post(new UpdateLivingEntityEvent.Pre(entity));
    }

    @Inject(method = "baseTick", at = @At("RETURN"))
    public void baseTickReturn(CallbackInfo ci) {
        Ares.EVENT_MANAGER.post(new UpdateLivingEntityEvent.Post(entity));
    }

    @Inject(method = "hasStatusEffect", at = @At("RETURN"), cancellable = true)
    public void hasStatusEffect(StatusEffect effect, CallbackInfoReturnable<Boolean> cir) {
        if(Ares.EVENT_MANAGER.post(new StatusEffectEvent(entity, effect)).isCancelled()) cir.setReturnValue(false);
    }

    @Inject(method = "jump", at = @At("HEAD"), cancellable = true)
    public void onJump(CallbackInfo ci) {
        if(entity != MC.player) return;
        if(Ares.EVENT_MANAGER.post(new PlayerJumpEvent()).isCancelled()) ci.cancel();
    }
}
