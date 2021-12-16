package dev.tigr.ares.fabric.impl.util;

import dev.tigr.ares.core.util.interfaces.IInv;
import dev.tigr.ares.fabric.mixin.accessors.ClientPlayerInteractionManagerAccessor;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.Item;

@SuppressWarnings("ConstantConditions")
public class CustomInv implements IInv {
    MinecraftClient MC = MinecraftClient.getInstance();
    @Override
    public int getCurrentSlot() {
        return MC.player.inventory.selectedSlot;
    }

    @Override
    public void setCurrentSlot(int currentSlot) {
        MC.player.inventory.selectedSlot = currentSlot;
    }

    @Override
    public void setLastSelectedSlot(int lastSelectedSlot) {
        ((ClientPlayerInteractionManagerAccessor) MC.interactionManager).setLastSelectedSlot(lastSelectedSlot);
    }

    @Override
    public int getItemInSlot(int slot) {
        return Item.getRawId(MC.player.inventory.getStack(slot).getItem());
    }

    @Override
    public int getMainHandItem() {
        return Item.getRawId(MC.player.getMainHandStack().getItem());
    }

    @Override
    public int getOffHandItem() {
        return Item.getRawId(MC.player.getOffHandStack().getItem());
    }
}
