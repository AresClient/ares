package org.aresclient.ares.mixin.mixins;

import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.state.EntityRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import org.aresclient.ares.api.JWrapper;
import org.aresclient.ares.api.events.RenderEntityLabelEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityRenderer.class)
public class MixinEntityRenderer implements JWrapper {
	@Inject(method = "renderLabelIfPresent", at = @At("HEAD"), cancellable = true)
	private <S extends EntityRenderState> void onRenderLabel(S state, Text text, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, CallbackInfo ci) {
		if (EVENTS.post(new RenderEntityLabelEvent(text)).isCancelled()) ci.cancel();
	}
}
