package org.aresclient.ares.mixin.mixins;

import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.feature.ArmorFeatureRenderer;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.client.render.entity.state.BipedEntityRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ItemStack;
import org.aresclient.ares.impl.instrument.modules.render.NoRender;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ArmorFeatureRenderer.class)
public class MixinArmorFeatureRenderer<S extends BipedEntityRenderState, M extends BipedEntityModel<S>, A extends BipedEntityModel<S>> {
    @Inject(method = "renderArmor", at = @At("HEAD"), cancellable = true)
    public void renderArmor(MatrixStack matrices, VertexConsumerProvider vertexConsumers, ItemStack stack, EquipmentSlot slot, int light, A armorModel, CallbackInfo ci) {
        if((slot == EquipmentSlot.HEAD && NoRender.INSTANCE.shouldBlockArmorHead())
        || (slot == EquipmentSlot.CHEST && NoRender.INSTANCE.shouldBlockArmorChest())
        || (slot == EquipmentSlot.LEGS && NoRender.INSTANCE.shouldBlockArmorLegs())
        || (slot == EquipmentSlot.FEET && NoRender.INSTANCE.shouldBlockArmorFeet()))
            ci.cancel();
    }
}
