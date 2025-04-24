package org.aresclient.ares.mixin.mixins;

import net.minecraft.client.render.LightmapTextureManager;
import org.aresclient.ares.impl.instrument.module.modules.render.Fullbright;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(LightmapTextureManager.class)
public class MixinLightmapTextureManager {
    @Redirect(method = "update", at = @At(value = "INVOKE", target = "Ljava/lang/Double;floatValue()F", ordinal = 1))
    private float getGamma(Double instance) {
        if(Fullbright.INSTANCE.isEnabled()) return 100f;
        return instance.floatValue();
    }
}
