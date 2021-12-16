package dev.tigr.ares.forge.impl.util;

import dev.tigr.ares.core.util.interfaces.IInv;
import dev.tigr.ares.forge.mixin.accessor.PlayerControllerMPAccessor;
import net.minecraft.client.Minecraft;
import net.minecraft.item.Item;

@SuppressWarnings("ConstantConditions")
public class CustomInv implements IInv {
    Minecraft MC = Minecraft.getMinecraft();
    @Override
    public int getCurrentSlot() {
        return MC.player.inventory.currentItem;
    }

    @Override
    public void setCurrentSlot(int currentSlot) {
        MC.player.inventory.currentItem = currentSlot;
    }

    @Override
    public void setLastSelectedSlot(int lastSelectedSlot) {
        ((PlayerControllerMPAccessor) MC.playerController).setLastSelectedSlot(lastSelectedSlot);
    }

    @Override
    public int getItemInSlot(int slot) {
        return Item.getIdFromItem(MC.player.inventory.getStackInSlot(slot).getItem());
    }

    @Override
    public int getMainHandItem() {
        return Item.getIdFromItem(MC.player.getHeldItemMainhand().getItem());
    }

    @Override
    public int getOffHandItem() {
        return Item.getIdFromItem(MC.player.getHeldItemOffhand().getItem());
    }
}
