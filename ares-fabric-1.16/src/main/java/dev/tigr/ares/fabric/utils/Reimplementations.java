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
import net.minecraft.client.util.math.Vector3f;
import net.minecraft.item.CrossbowItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;
import net.minecraft.util.math.MathHelper;

/**
 * This just holds methods taken from minecraft which had to be modified and are used with mixins
 */
public class Reimplementations {
    /**
     * net.minecraft.client.render.item.HeldItemRenderer#renderItem(float, net.minecraft.client.util.math.MatrixStack, net.minecraft.client.render.VertexConsumerProvider.Immediate, net.minecraft.client.network.ClientPlayerEntity, int)
     * MixinGameRenderer
     */
    public static  void renderItem(HeldItemRenderer heldItemRenderer, float tickDelta, MatrixStack matrices, VertexConsumerProvider.Immediate vertexConsumers, ClientPlayerEntity player, int light) {
        float f = player.getHandSwingProgress(tickDelta);
        Hand hand = MoreObjects.firstNonNull(player.preferredHand, Hand.MAIN_HAND);
        float g = MathHelper.lerp(tickDelta, player.prevPitch, player.pitch);
        boolean bl = true;
        boolean bl2 = true;
        ItemStack itemStack3;
        if (player.isUsingItem()) {
            itemStack3 = player.getActiveItem();
            if (itemStack3.getItem() == Items.BOW || itemStack3.getItem() == Items.CROSSBOW) {
                bl = player.getActiveHand() == Hand.MAIN_HAND;
                bl2 = !bl;
            }

            Hand hand2 = player.getActiveHand();
            if (hand2 == Hand.MAIN_HAND) {
                ItemStack itemStack2 = player.getOffHandStack();
                if (itemStack2.getItem() == Items.CROSSBOW && CrossbowItem.isCharged(itemStack2)) {
                    bl2 = false;
                }
            }
        } else {
            itemStack3 = player.getMainHandStack();
            ItemStack itemStack4 = player.getOffHandStack();
            if (itemStack3.getItem() == Items.CROSSBOW && CrossbowItem.isCharged(itemStack3)) {
                bl2 = !bl;
            }

            if (itemStack4.getItem() == Items.CROSSBOW && CrossbowItem.isCharged(itemStack4)) {
                bl = !itemStack3.isEmpty();
                bl2 = !bl;
            }
        }

        float h = MathHelper.lerp(tickDelta, player.lastRenderPitch, player.renderPitch);
        float i = MathHelper.lerp(tickDelta, player.lastRenderYaw, player.renderYaw);
        matrices.multiply(Vector3f.POSITIVE_X.getDegreesQuaternion((player.getPitch(tickDelta) - h) * 0.1F));
        matrices.multiply(Vector3f.POSITIVE_Y.getDegreesQuaternion((player.getYaw(tickDelta) - i) * 0.1F));
        float m;
        float l;

        if (bl) {
            matrices.push(); // Save a backup of current matrices

            RenderHeldItemEvent.Cancelled e = Ares.EVENT_MANAGER.post(new RenderHeldItemEvent.Cancelled(Hand.MAIN_HAND, matrices));

            l = hand == Hand.MAIN_HAND ? f : 0.0F;
            m = 1.0F - MathHelper.lerp(tickDelta, ((HeldItemRendererAccessor)heldItemRenderer).getPrevEquipProgressMainHand(), ((HeldItemRendererAccessor)heldItemRenderer).getEquipProgressMainHand());
            ((IHeldItemRenderer)heldItemRenderer).doRenderFirstPersonItem(player, tickDelta, g, Hand.MAIN_HAND, l, ((HeldItemRendererAccessor) heldItemRenderer).getMainHand(), m, e.getMatrices(), vertexConsumers, light);

            matrices.pop(); // Use saved backup to get rid of transformations from main hand
        }

        if (bl2) {
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
