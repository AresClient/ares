package org.aresclient.ares.mixin.mixins;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.render.model.BakedQuad;
import net.minecraft.client.util.math.MatrixStack;
import org.aresclient.ares.impl.instrument.module.components.render.esp.Chamlike;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.ArrayList;
import java.util.List;

@Mixin(ItemRenderer.class)
public abstract class MixinItemRenderer {
	@WrapOperation(
			method = "renderItem(" +
					"Lnet/minecraft/item/ItemDisplayContext;" +
					"Lnet/minecraft/client/util/math/MatrixStack;" +
					"Lnet/minecraft/client/render/VertexConsumerProvider;" +
					"II[ILjava/util/List;Lnet/minecraft/client/render/RenderLayer;" +
					"Lnet/minecraft/client/render/item/ItemRenderState$Glint;" +
					")V",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/client/render/item/ItemRenderer;" +
							"renderBakedItemQuads(Lnet/minecraft/client/util/math/MatrixStack;" +
							"Lnet/minecraft/client/render/VertexConsumer;" +
							"Ljava/util/List;" +
							"[III)V"
			)
	)
	private static void onRenderItem(MatrixStack matrices, VertexConsumer vertexConsumer, List<BakedQuad> quads, int[] tints, int light, int overlay, Operation<Void> original) {
		if (!Chamlike.INSTANCE.getActive() || Chamlike.INSTANCE.isBlockItem()) {
			original.call(matrices, vertexConsumer, quads, tints, light, overlay);
			return;
		}

		// Skip the first quads which are used to texture the sides because they just look like squares with Chamlike ESP

		ArrayList<BakedQuad> newList = new ArrayList<>();
		int i = 0;

		for (BakedQuad bakedQuad: quads) {
			if (++i <= 2) continue;
			newList.add(bakedQuad);
		}

		original.call(matrices, vertexConsumer, newList, tints, light, overlay);
	}
}
