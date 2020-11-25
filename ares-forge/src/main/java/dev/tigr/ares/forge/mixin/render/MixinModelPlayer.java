package dev.tigr.ares.forge.mixin.render;

import net.minecraft.client.model.ModelPlayer;
import net.minecraft.client.renderer.GlStateManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * @author Tigermouthbear
 */
@Mixin(ModelPlayer.class)
public class MixinModelPlayer {
    @Inject(method = "renderCape", at = @At("RETURN"))
    public void resetColor(float scale, CallbackInfo ci) {
        GlStateManager.color(1, 1, 1, 1);
    }
}
