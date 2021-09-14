package dev.tigr.ares.fabric.utils;

import com.google.common.base.MoreObjects;
import dev.tigr.ares.core.Ares;
import dev.tigr.ares.fabric.event.render.RenderHeldItemEvent;
import dev.tigr.ares.fabric.mixin.accessors.HeldItemRendererAccessor;
import dev.tigr.ares.fabric.mixininterface.IHeldItemRenderer;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.item.HeldItemRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Hand;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3f;

/**
 * This just holds methods taken from minecraft which had to be modified and are used with mixins
 */
public class Reimplementations {
    /**
     * net.minecraft.client.render.item.HeldItemRenderer.renderItem(float, net.minecraft.client.util.math.MatrixStack, net.minecraft.client.render.VertexConsumerProvider.Immediate, net.minecraft.client.network.ClientPlayerEntity, int)
     * MixinGameRenderer
     */
    public static void renderItem(HeldItemRenderer heldItemRenderer, float tickDelta, MatrixStack matrices, VertexConsumerProvider.Immediate vertexConsumers, ClientPlayerEntity player, int light) {
        float f = player.getHandSwingProgress(tickDelta);
        Hand hand = MoreObjects.firstNonNull(player.preferredHand, Hand.MAIN_HAND);
        float g = MathHelper.lerp(tickDelta, player.prevPitch, player.getPitch());
        float h = MathHelper.lerp(tickDelta, player.lastRenderPitch, player.renderPitch);
        float i = MathHelper.lerp(tickDelta, player.lastRenderYaw, player.renderYaw);
        matrices.multiply(Vec3f.POSITIVE_X.getDegreesQuaternion((player.getPitch(tickDelta) - h) * 0.1F));
        matrices.multiply(Vec3f.POSITIVE_Y.getDegreesQuaternion((player.getYaw(tickDelta) - i) * 0.1F));
        float l;
        float m;

        if(!player.getMainHandStack().isEmpty()) {
            matrices.push(); // Save a backup of current matrices

            RenderHeldItemEvent.Cancelled e = Ares.EVENT_MANAGER.post(new RenderHeldItemEvent.Cancelled(Hand.MAIN_HAND, matrices));

            l = hand == Hand.MAIN_HAND ? f : 0.0F;
            m = 1.0F - MathHelper.lerp(tickDelta, ((HeldItemRendererAccessor)heldItemRenderer).getPrevEquipProgressMainHand(), ((HeldItemRendererAccessor)heldItemRenderer).getEquipProgressMainHand());
            ((IHeldItemRenderer)heldItemRenderer).doRenderFirstPersonItem(player, tickDelta, g, Hand.MAIN_HAND, l, ((HeldItemRendererAccessor) heldItemRenderer).getMainHand(), m, e.getMatrices(), vertexConsumers, light);

            matrices.pop(); // Use saved backup to get rid of transformations from main hand
        }

        if(!player.getOffHandStack().isEmpty()) {
            matrices.push();

            RenderHeldItemEvent.Cancelled e = Ares.EVENT_MANAGER.post(new RenderHeldItemEvent.Cancelled(Hand.OFF_HAND, matrices));

            l = hand == Hand.OFF_HAND ? f : 0.0F;
            m = 1.0F - MathHelper.lerp(tickDelta, ((HeldItemRendererAccessor) heldItemRenderer).getPrevEquipProgressOffHand(), ((HeldItemRendererAccessor)heldItemRenderer).getEquipProgressOffHand());
            ((IHeldItemRenderer)heldItemRenderer).doRenderFirstPersonItem(player, tickDelta, g, Hand.OFF_HAND, l, ((HeldItemRendererAccessor)heldItemRenderer).getOffHand(), m, e.getMatrices(), vertexConsumers, light);

            matrices.pop();
        }

        vertexConsumers.draw();
    }
}
