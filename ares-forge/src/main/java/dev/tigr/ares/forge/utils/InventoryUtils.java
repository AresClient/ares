package dev.tigr.ares.forge.utils;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;

import static dev.tigr.ares.Wrapper.MC;

/**
 * @author Tigermouthbear
 */
public class InventoryUtils {
    public static int amountInInventory(Item item) {
        int quantity = 0;

        for(int i = 44; i > -1; i--) {
            ItemStack stackInSlot = MC.player.inventoryContainer.getSlot(i).getStack();
            if(stackInSlot.getItem() == item) quantity += stackInSlot.getCount();
        }
        if(MC.player.getHeldItemOffhand().getItem() == item) quantity += MC.player.getHeldItemOffhand().getCount();

        return quantity;
    }

    public static int amountInHotbar(Item item) {
        int quantity = 0;

        for(int i = 44; i > 35; i--) {
            ItemStack stackInSlot = MC.player.inventoryContainer.getSlot(i).getStack();
            if(stackInSlot.getItem() == item) quantity += stackInSlot.getCount();
        }
        if(MC.player.getHeldItemOffhand().getItem() == item) quantity += MC.player.getHeldItemOffhand().getCount();

        return quantity;
    }

    public static int amountBlockInHotbar(Block block) {return amountInHotbar(new ItemStack(block).getItem());}

    public static int findItem(Item item) {
        int index = -1;
        for(int i = 44; i > -1; i--) {
            if(MC.player.inventory.getStackInSlot(i).getItem() == item) {
                index = i;
                break;
            }
        }
        return index;
    }

    public static int findBlock(Block block) {
        return findItem(new ItemStack(block).getItem());
    }

    public static int findItemInHotbar(Item item) {
        int index = -1;
        for(int i = 0; i < 9; i++) {
            if(MC.player.inventory.getStackInSlot(i).getItem() == item) {
                index = i;
                break;
            }
        }
        return index;
    }

    public static int findBlockInHotbar(Block block) {
        return findItemInHotbar(new ItemStack(block).getItem());
    }

    public static int getBlockInHotbar() {
        for(int i = 0; i < 9; i++) {
            if(
                    MC.player.inventory.getStackInSlot(i) == ItemStack.EMPTY
                            || !(MC.player.inventory.getStackInSlot(i).getItem() instanceof ItemBlock)
                            || !Block.getBlockFromItem(MC.player.inventory.getStackInSlot(i).getItem()).getDefaultState().isFullBlock()
            ) continue;

            return i;
        }

        return -1;
    }

    public static int getBlank() {
        int index = -1;
        for(int i = 44; i > -1; i--) {
            if(MC.player.inventory.getStackInSlot(i).isEmpty()) {
                index = i;
                break;
            }
        }
        return index;
    }

    public static int getSlotIndex(int index) {
        return index < 9 ? index + 36 : index;
    }
}
