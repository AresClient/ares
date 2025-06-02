package org.aresclient.ares.mixin.mixins;

import net.minecraft.client.gui.hud.InGameOverlayRenderer;
import org.aresclient.ares.impl.instrument.modules.render.NoRender;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InGameOverlayRenderer.class)
public class MixinInGameOverlayRenderer {
    @Inject(method = "renderFireOverlay", at = @At("HEAD"), cancellable = true)
    private static void renderFireOverlay(CallbackInfo ci) {
        if(NoRender.INSTANCE.shouldBlockFireOverlay()) ci.cancel();
    }

    @Inject(method = "renderUnderwaterOverlay", at = @At("HEAD"), cancellable = true)
    private static void renderUnderwaterOverlay(CallbackInfo ci) {
        if(NoRender.INSTANCE.shouldBlockWaterOverlay()) ci.cancel();
    }

    @Inject(method = "renderInWallOverlay", at = @At("HEAD"), cancellable = true)
    private static void renderInWallOverlay(CallbackInfo ci) {
        if(NoRender.INSTANCE.shouldBlockWallOverlay()) ci.cancel();
    }
}
