package dev.tigr.ares.forge.mixin.render;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.layers.LayerElytra;
import net.minecraft.entity.EntityLivingBase;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Fixes rainbow cape color bleeding
 *
 * @author Tigermouthbear
 */
@Mixin(LayerElytra.class)
public class MixinLayerElytra {
    @Inject(method = "doRenderLayer", at = @At("RETURN"))
    public void postElytraRender(EntityLivingBase entitylivingbaseIn, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch, float scale, CallbackInfo ci) {
        GlStateManager.color(1, 1, 1, 1);
    }
}
