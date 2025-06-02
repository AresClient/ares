package org.aresclient.ares.mixin.mixins;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.util.Identifier;
import org.aresclient.ares.api.JWrapper;
import org.aresclient.ares.impl.instrument.modules.render.NoRender;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InGameHud.class)
public class MixinInGameHud implements JWrapper {
    @Shadow @Final private static Identifier POWDER_SNOW_OUTLINE;

    @Inject(method = "renderPortalOverlay", at = @At("HEAD"), cancellable = true)
    public void renderPortalOverlay(CallbackInfo ci) {
        if(NoRender.INSTANCE.shouldBlockPortalOverlay()) ci.cancel();
    }

    @Inject(method = "renderNauseaOverlay", at = @At("HEAD"), cancellable = true)
    public void renderNauseaOverlay(CallbackInfo ci) {
        if(NoRender.INSTANCE.shouldBlockNausea()) ci.cancel();
    }

    @Inject(method = "renderOverlay", at = @At("HEAD"), cancellable = true)
    public void renderOverlay(DrawContext context, Identifier texture, float opacity, CallbackInfo ci) {
        if(texture == POWDER_SNOW_OUTLINE && NoRender.INSTANCE.shouldBlockPowderSnowOverlay()) ci.cancel();
    }
}
