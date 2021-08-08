package dev.tigr.ares.fabric.mixin.render;

import dev.tigr.ares.core.Ares;
import dev.tigr.ares.fabric.event.render.GammaEvent;
import net.minecraft.client.options.GameOptions;
import net.minecraft.client.render.LightmapTextureManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(LightmapTextureManager.class)
public class MixinLightmapTextureManager {
    @Redirect(method = "update", at = @At(value = "FIELD", target = "Lnet/minecraft/client/options/GameOptions;gamma:D"))
    public double getGamma(GameOptions gameOptions) {
        return Ares.EVENT_MANAGER.post(new GammaEvent(gameOptions.gamma)).getGamma();
    }
}
