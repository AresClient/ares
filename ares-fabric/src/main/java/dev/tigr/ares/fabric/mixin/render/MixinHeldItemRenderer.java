package dev.tigr.ares.fabric.mixin.render;

import com.google.common.base.MoreObjects;
import dev.tigr.ares.core.Ares;
import dev.tigr.ares.fabric.event.render.RenderHeldItemEvent;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.item.HeldItemRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HeldItemRenderer.class)
public abstract class MixinHeldItemRenderer {
    @Shadow private float prevEquipProgressOffHand;

    @Shadow private float equipProgressOffHand;

    @Shadow private ItemStack offHand;

    @Shadow private float prevEquipProgressMainHand;

    @Shadow private float equipProgressMainHand;

    @Shadow private ItemStack mainHand;

    @Shadow protected abstract void renderFirstPersonItem(AbstractClientPlayerEntity player, float tickDelta, float pitch, Hand hand, float swingProgress, ItemStack item, float equipProgress, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light);

    @Inject(method = "renderItem(FLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider$Immediate;Lnet/minecraft/client/network/ClientPlayerEntity;I)V", at = @At("HEAD"), cancellable = true)
    public void onRenderItem(float tickDelta, MatrixStack matrices, VertexConsumerProvider.Immediate vertexConsumers, ClientPlayerEntity player, int light, CallbackInfo ci) {
        if(!Ares.EVENT_MANAGER.post(new RenderHeldItemEvent.Invoke()).isCancelled()) return;

        ci.cancel();

        float f = player.getHandSwingProgress(tickDelta);
        Hand hand = MoreObjects.firstNonNull(player.preferredHand, Hand.MAIN_HAND);
        float g = MathHelper.lerp(tickDelta, player.prevPitch, player.getPitch());
        float h = MathHelper.lerp(tickDelta, player.lastRenderPitch, player.renderPitch);
        float i = MathHelper.lerp(tickDelta, player.lastRenderYaw, player.renderYaw);
        matrices.multiply(Vec3f.POSITIVE_X.getDegreesQuaternion((player.getPitch(tickDelta) - h) * 0.1F));
        matrices.multiply(Vec3f.POSITIVE_Y.getDegreesQuaternion((player.getYaw(tickDelta) - i) * 0.1F));
        float l;
        float m;

        matrices.push(); // Save a backup of current matrices

        RenderHeldItemEvent.Cancelled e1 = Ares.EVENT_MANAGER.post(new RenderHeldItemEvent.Cancelled(Hand.MAIN_HAND, matrices));

        l = hand == Hand.MAIN_HAND ? f : 0.0F;
        m = 1.0F - MathHelper.lerp(tickDelta, prevEquipProgressMainHand, equipProgressMainHand);
        renderFirstPersonItem(player, tickDelta, g, Hand.MAIN_HAND, l, mainHand, m, e1.getMatrices(), vertexConsumers, light);

        matrices.pop(); // Use saved backup to get rid of transformations from main hand

        if(!player.getOffHandStack().isEmpty()) {
            matrices.push();

            RenderHeldItemEvent.Cancelled e2 = Ares.EVENT_MANAGER.post(new RenderHeldItemEvent.Cancelled(Hand.OFF_HAND, matrices));

            l = hand == Hand.OFF_HAND ? f : 0.0F;
            m = 1.0F - MathHelper.lerp(tickDelta, prevEquipProgressOffHand, equipProgressOffHand);
            renderFirstPersonItem(player, tickDelta, g, Hand.OFF_HAND, l, offHand, m, e2.getMatrices(), vertexConsumers, light);

            matrices.pop();
        }

        vertexConsumers.draw();
    }
}
