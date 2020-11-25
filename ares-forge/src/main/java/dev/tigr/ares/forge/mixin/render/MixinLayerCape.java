package dev.tigr.ares.forge.mixin.render;

import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.layers.LayerCape;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Fixes rainbow cape color bleeding
 *
 * @author Tigermouthbear
 */
@Mixin(LayerCape.class)
public class MixinLayerCape {
    @Inject(method = "doRenderLayer", at = @At("RETURN"))
    public void postCapeRender(AbstractClientPlayer entitylivingbaseIn, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch, float scale, CallbackInfo ci) {
        GlStateManager.color(1, 1, 1, 1);
    }
}
