package dev.tigr.ares.fabric.mixin.render;

import dev.tigr.ares.core.Ares;
import dev.tigr.ares.core.feature.module.Module;
import dev.tigr.ares.fabric.event.render.HurtCamEvent;
import net.minecraft.client.render.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * @author Tigermouthbear 8/7/20
 */
@Mixin(GameRenderer.class)
public class MixinGameRenderer {
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
