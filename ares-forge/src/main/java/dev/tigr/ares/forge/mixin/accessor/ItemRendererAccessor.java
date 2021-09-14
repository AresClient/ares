package dev.tigr.ares.forge.mixin.accessor;

import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ItemRenderer.class)
public interface ItemRendererAccessor {
    @Accessor("itemStackMainHand")
    ItemStack getItemStackMainHand();

    @Accessor("itemStackOffHand")
    ItemStack getItemStackOffHand();

    @Accessor("equippedProgressMainHand")
    float getEquippedProgressMainHand();

    @Accessor("prevEquippedProgressMainHand")
    float getPrevEquippedProgressMainHand();

    @Accessor("equippedProgressOffHand")
    float getEquippedProgressOffHand();

    @Accessor("prevEquippedProgressOffHand")
    float getPrevEquippedProgressOffHand();
}
