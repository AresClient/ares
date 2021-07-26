package dev.tigr.ares.fabric.mixin.render;

import dev.tigr.ares.core.Ares;
import dev.tigr.ares.core.util.render.Color;
import dev.tigr.ares.fabric.event.render.CapeEvent;
import dev.tigr.ares.fabric.mixin.accessors.PlayerEntityModelAccessor;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.PlayerModelPart;
import net.minecraft.client.render.entity.feature.CapeFeatureRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3f;
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

    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    public void render(MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i, AbstractClientPlayerEntity abstractClientPlayerEntity, float f, float g, float h, float j, float k, float l, CallbackInfo ci){
        CapeEvent event = Ares.EVENT_MANAGER.post(new CapeEvent(abstractClientPlayerEntity));
        Color color = event.getColor() == null ? Color.WHITE : event.getColor();

        if(abstractClientPlayerEntity.canRenderCapeTexture() && !abstractClientPlayerEntity.isInvisible() && abstractClientPlayerEntity.isPartVisible(PlayerModelPart.CAPE) && event.getIdentifier() != null) {
            ItemStack itemStack = abstractClientPlayerEntity.getEquippedStack(EquipmentSlot.CHEST);
            if(!itemStack.isOf(Items.ELYTRA)) {
                matrixStack.push();
                matrixStack.translate(0.0D, 0.0D, 0.125D);
                double d = MathHelper.lerp(h, abstractClientPlayerEntity.prevCapeX, abstractClientPlayerEntity.capeX) - MathHelper.lerp(h, abstractClientPlayerEntity.prevX, abstractClientPlayerEntity.getX());
                double e = MathHelper.lerp(h, abstractClientPlayerEntity.prevCapeY, abstractClientPlayerEntity.capeY) - MathHelper.lerp(h, abstractClientPlayerEntity.prevY, abstractClientPlayerEntity.getY());
                double m = MathHelper.lerp(h, abstractClientPlayerEntity.prevCapeZ, abstractClientPlayerEntity.capeZ) - MathHelper.lerp(h, abstractClientPlayerEntity.prevZ, abstractClientPlayerEntity.getZ());
                float n = abstractClientPlayerEntity.prevBodyYaw + (abstractClientPlayerEntity.bodyYaw - abstractClientPlayerEntity.prevBodyYaw);
                double o = MathHelper.sin(n * 0.017453292F);
                double p = -MathHelper.cos(n * 0.017453292F);
                float q = (float)e * 10.0F;
                q = MathHelper.clamp(q, -6.0F, 32.0F);
                float r = (float)(d * o + m * p) * 100.0F;
                r = MathHelper.clamp(r, 0.0F, 150.0F);
                float s = (float)(d * p - m * o) * 100.0F;
                s = MathHelper.clamp(s, -20.0F, 20.0F);
                if (r < 0.0F) {
                    r = 0.0F;
                }

                float t = MathHelper.lerp(h, abstractClientPlayerEntity.prevStrideDistance, abstractClientPlayerEntity.strideDistance);
                q += MathHelper.sin(MathHelper.lerp(h, abstractClientPlayerEntity.prevHorizontalSpeed, abstractClientPlayerEntity.horizontalSpeed) * 6.0F) * 32.0F * t;
                if (abstractClientPlayerEntity.isInSneakingPose()) {
                    q += 25.0F;
                }

                matrixStack.multiply(Vec3f.POSITIVE_X.getDegreesQuaternion(6.0F + r / 2.0F + q));
                matrixStack.multiply(Vec3f.POSITIVE_Z.getDegreesQuaternion(s / 2.0F));
                matrixStack.multiply(Vec3f.POSITIVE_Y.getDegreesQuaternion(180.0F - s / 2.0F));
                VertexConsumer vertexConsumer = vertexConsumerProvider.getBuffer(RenderLayer.getEntitySolid(event.getIdentifier()));
                ((PlayerEntityModelAccessor) capeFeatureRenderer.getContextModel()).getCloak().render(matrixStack, vertexConsumer, i, OverlayTexture.DEFAULT_UV, color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha());
                matrixStack.pop();
            }
            ci.cancel();
        }
    }
}
