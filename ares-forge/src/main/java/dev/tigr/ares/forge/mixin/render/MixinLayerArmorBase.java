package dev.tigr.ares.forge.mixin.render;

import dev.tigr.ares.core.Ares;
import dev.tigr.ares.forge.event.events.render.ArmorRenderEvent;
import net.minecraft.client.renderer.entity.layers.LayerArmorBase;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.inventory.EntityEquipmentSlot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * @author Tigermouthbear
 */
@Mixin(LayerArmorBase.class)
public class MixinLayerArmorBase {
    @Inject(method = "renderArmorLayer", at = @At("HEAD"), cancellable = true)
    public void armorRenderWrapper(EntityLivingBase entityLivingBaseIn, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch, float scale, EntityEquipmentSlot slotIn, CallbackInfo ci) {
        ArmorRenderEvent event = Ares.EVENT_MANAGER.post(new ArmorRenderEvent());
        switch(slotIn) {
            case HEAD:
                if(event.shouldRenderHat()) ci.cancel();
                break;

            case CHEST:
                if(event.shouldRenderShirt()) ci.cancel();
                break;

            case LEGS:
                if(event.shouldRenderPants()) ci.cancel();
                break;

            case FEET:
                if(event.shouldRenderShoes()) ci.cancel();
                break;
        }
    }
}
