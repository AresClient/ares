package dev.tigr.ares.fabric.mixin.render;

import dev.tigr.ares.core.Ares;
import dev.tigr.ares.core.feature.module.Module;
import dev.tigr.ares.fabric.event.player.AntiHitboxEvent;
import dev.tigr.ares.fabric.event.player.CanHandCollideWaterEvent;
import dev.tigr.ares.fabric.event.render.HurtCamEvent;
import dev.tigr.simpleevents.event.Result;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Predicate;

/**
 * @author Tigermouthbear 8/7/20
 */
@Mixin(GameRenderer.class)
public class MixinGameRenderer {
    @Redirect(method = "updateTargetedEntity", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;raycast(DFZ)Lnet/minecraft/util/hit/HitResult;"))
    public HitResult liquidInteract(Entity entity, double maxDistance, float tickDelta, boolean includeFluids) {
        if(Ares.EVENT_MANAGER.post(new CanHandCollideWaterEvent()).getResult() == Result.ALLOW) return entity.raycast(maxDistance, tickDelta, true);
        return entity.raycast(maxDistance, tickDelta, includeFluids);
    }

    @Redirect(method = "updateTargetedEntity", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/projectile/ProjectileUtil;raycast(Lnet/minecraft/entity/Entity;Lnet/minecraft/util/math/Vec3d;Lnet/minecraft/util/math/Vec3d;Lnet/minecraft/util/math/Box;Ljava/util/function/Predicate;D)Lnet/minecraft/util/hit/EntityHitResult;"))
    public EntityHitResult updateTargetedEntity(Entity entity, Vec3d vec3d, Vec3d vec3d1, Box box, Predicate<Entity> predicate, double d) {
        if(Ares.EVENT_MANAGER.post(new AntiHitboxEvent()).getResult() == Result.ALLOW) return null;
        return ProjectileUtil.raycast(entity, vec3d, vec3d1, box, predicate, d);
    }

    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/hud/InGameHud;render(Lnet/minecraft/client/util/math/MatrixStack;F)V"))
    private void render(float tickDelta, long startTime, boolean tick, CallbackInfo ci) {
        Module.render2d();
    }

    @Inject(method = "renderHand", at = @At("HEAD"))
    public void renderWorld(CallbackInfo ci) {
        Module.render3d();
    }

    @Inject(method = "bobViewWhenHurt", at = @At("HEAD"), cancellable = true)
    public void hurtShake(CallbackInfo ci) {
        if(Ares.EVENT_MANAGER.post(new HurtCamEvent()).isCancelled()) ci.cancel();
    }
}
