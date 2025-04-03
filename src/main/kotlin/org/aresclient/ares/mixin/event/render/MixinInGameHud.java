package org.aresclient.ares.mixin.event.render;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.render.RenderTickCounter;
import org.aresclient.ares.api.JWrapper;
import org.aresclient.ares.api.events.RenderEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InGameHud.class)
public class MixinInGameHud implements JWrapper {
    @Inject(method = "render", at = @At("TAIL"))
    public void renderPost(DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci) {
        context.draw();
        EVENTS.post(new RenderEvent.Hud(tickCounter.getFixedDeltaTicks()));
    }
}
