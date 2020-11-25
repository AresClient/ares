package dev.tigr.ares.fabric.mixin.render;

import dev.tigr.ares.core.Ares;
import dev.tigr.ares.fabric.event.render.RenderOverlaysEvent;
import net.minecraft.client.gui.hud.InGameOverlayRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * @author Tigermouthbear 8/30/20
 */
@Mixin(InGameOverlayRenderer.class)
public class MixinInGameOverlayRenderer {
    @Inject(method = "renderInWallOverlay", at = @At("HEAD"), cancellable = true)
    private static void renderInWallOverlay(CallbackInfo ci) {
        if(Ares.EVENT_MANAGER.post(new RenderOverlaysEvent(RenderOverlaysEvent.Type.BLOCK)).isCancelled()) ci.cancel();
    }

    @Inject(method = "renderUnderwaterOverlay", at = @At("HEAD"), cancellable = true)
    private static void renderUnderwaterOverlay(CallbackInfo ci) {
        if(Ares.EVENT_MANAGER.post(new RenderOverlaysEvent(RenderOverlaysEvent.Type.WATER)).isCancelled()) ci.cancel();
    }

    @Inject(method = "renderFireOverlay", at = @At("HEAD"), cancellable = true)
    private static void renderFireOverlay(CallbackInfo ci) {
        if(Ares.EVENT_MANAGER.post(new RenderOverlaysEvent(RenderOverlaysEvent.Type.FIRE)).isCancelled()) ci.cancel();
    }
}
