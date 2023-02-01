package org.aresclient.ares.mixins;

import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.util.math.MatrixStack;
import org.aresclient.ares.Ares;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InGameHud.class)
public class MixinInGameHud {
    @Inject(method = "render", at = @At(value = "FIELD", target = "Lnet/minecraft/client/option/GameOptions;heldItemTooltips:Z"))
    public void renderOverlay(MatrixStack matrices, float tickDelta, CallbackInfo ci) {
        Ares.Companion.renderHud(tickDelta);
    }
}
