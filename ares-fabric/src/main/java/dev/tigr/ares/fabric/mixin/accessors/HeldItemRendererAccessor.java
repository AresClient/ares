package dev.tigr.ares.fabric.mixin.accessors;

import net.minecraft.client.render.item.HeldItemRenderer;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(HeldItemRenderer.class)
public interface HeldItemRendererAccessor {
    @Accessor("mainHand")
    ItemStack getMainHand();

    @Accessor("offHand")
    ItemStack getOffHand();

    @Accessor("equipProgressMainHand")
    float getEquipProgressMainHand();

    @Accessor("prevEquipProgressMainHand")
    float getPrevEquipProgressMainHand();

    @Accessor("equipProgressOffHand")
    float getEquipProgressOffHand();

    @Accessor("prevEquipProgressOffHand")
    float getPrevEquipProgressOffHand();
}
