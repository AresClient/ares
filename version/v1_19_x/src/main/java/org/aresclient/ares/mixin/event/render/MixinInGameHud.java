package org.aresclient.ares.mixin.event.render;

import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.util.math.MatrixStack;
import org.aresclient.ares.api.Ares;
import org.aresclient.ares.api.event.render.RenderEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InGameHud.class)
public class MixinInGameHud {
    @Inject(method = "render", at = @At("RETURN"))
    public void renderPost(MatrixStack matrices, float tickDelta, CallbackInfo ci) {
        Ares.getEventManager().post(new RenderEvent.Hud(tickDelta));
    }
}
