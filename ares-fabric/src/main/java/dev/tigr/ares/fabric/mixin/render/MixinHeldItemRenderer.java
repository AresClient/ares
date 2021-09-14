package dev.tigr.ares.fabric.mixin.render;

import dev.tigr.ares.fabric.mixininterface.IHeldItemRenderer;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.item.HeldItemRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(HeldItemRenderer.class)
public abstract class MixinHeldItemRenderer implements IHeldItemRenderer {
    @Shadow
    protected abstract void renderFirstPersonItem(
            AbstractClientPlayerEntity player, float tickDelta, float pitch, Hand hand, float swingProgress,
            ItemStack item, float equipProgress, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light
    );

    @Override
    public void doRenderFirstPersonItem(
            AbstractClientPlayerEntity player, float tickDelta, float pitch, Hand hand, float swingProgress,
            ItemStack item, float equipProgress, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light
    ) {
        renderFirstPersonItem(player, tickDelta, pitch, hand, swingProgress, item, equipProgress, matrices, vertexConsumers, light);
    }
}
