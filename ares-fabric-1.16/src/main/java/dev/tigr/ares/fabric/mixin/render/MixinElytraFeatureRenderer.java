package dev.tigr.ares.fabric.mixin.render;

import dev.tigr.ares.core.Ares;
import dev.tigr.ares.core.util.render.Color;
import dev.tigr.ares.fabric.event.render.CapeEvent;
import dev.tigr.ares.fabric.mixin.accessors.ElytraFeatureRendererAccessor;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.feature.ElytraFeatureRenderer;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * @author Tigermouthbear 10/18/20
 */
@Mixin(ElytraFeatureRenderer.class)
public class MixinElytraFeatureRenderer<T extends LivingEntity, M extends EntityModel<T>> {
    private final ElytraFeatureRenderer<T, M> elytraFeatureRenderer = (ElytraFeatureRenderer<T, M>) (Object) this;

    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/item/ItemRenderer;getArmorGlintConsumer(Lnet/minecraft/client/render/VertexConsumerProvider;Lnet/minecraft/client/render/RenderLayer;ZZ)Lnet/minecraft/client/render/VertexConsumer;"), cancellable = true)
    public void renderElytra(MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i, T livingEntity, float f, float f1, float f2, float f3, float f4, float f5, CallbackInfo ci) {
        if(livingEntity instanceof AbstractClientPlayerEntity) {
            CapeEvent event = Ares.EVENT_MANAGER.post(new CapeEvent((AbstractClientPlayerEntity) livingEntity));
            Color color = event.getColor() == null ? Color.WHITE : event.getColor();
            if(event.getIdentifier() != null) {
                ItemStack itemStack = livingEntity.getEquippedStack(EquipmentSlot.CHEST);
                VertexConsumer vertexConsumer = ItemRenderer.getArmorGlintConsumer(vertexConsumerProvider, RenderLayer.getArmorCutoutNoCull(event.getIdentifier()), false, itemStack.hasGlint());
                ((ElytraFeatureRendererAccessor) elytraFeatureRenderer).getElytra().render(matrixStack, vertexConsumer, i, OverlayTexture.DEFAULT_UV, color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha());
                matrixStack.pop();
                ci.cancel();
            }
        }
    }
}
