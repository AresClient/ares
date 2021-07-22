package dev.tigr.ares.fabric.mixin.render;

import dev.tigr.ares.core.Ares;
import dev.tigr.ares.fabric.event.render.CapeColorEvent;
import dev.tigr.ares.fabric.mixin.accessors.PlayerEntityModelAccessor;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.feature.CapeFeatureRenderer;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * @author Tigermouthbear 10/18/20
 */
@Mixin(CapeFeatureRenderer.class)
public abstract class MixinCapeFeatureRenderer {
    private final CapeFeatureRenderer capeFeatureRenderer = (CapeFeatureRenderer) (Object) this;

    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/RenderLayer;getEntitySolid(Lnet/minecraft/util/Identifier;)Lnet/minecraft/client/render/RenderLayer;"), cancellable = true)
    public void renderCape(MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i, AbstractClientPlayerEntity abstractClientPlayerEntity, float f, float f1, float f2, float f3, float f4, float f5, CallbackInfo ci) {
        CapeColorEvent event = Ares.EVENT_MANAGER.post(new CapeColorEvent(abstractClientPlayerEntity));
        if(event.getColor() != null) {
            VertexConsumer vertexConsumer = vertexConsumerProvider.getBuffer(RenderLayer.getEntitySolid(abstractClientPlayerEntity.getCapeTexture()));
            ((PlayerEntityModelAccessor) capeFeatureRenderer.getContextModel()).getCloak().render(matrixStack, vertexConsumer, i, OverlayTexture.DEFAULT_UV, event.getColor().getRed(), event.getColor().getGreen(), event.getColor().getBlue(), event.getColor().getAlpha());
            matrixStack.pop();
            ci.cancel();
        }
    }
}
