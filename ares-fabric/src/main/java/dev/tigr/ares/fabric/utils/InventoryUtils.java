package dev.tigr.ares.fabric.utils;

import dev.tigr.ares.Wrapper;
import net.minecraft.block.Block;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;

/**
 * @author Tigermouthbear
 */
public class InventoryUtils implements Wrapper {
    public static int amountInInventory(Item item) {
        int quantity = 0;

        for(int i = 0; i <= 44; i++) {
            ItemStack stackInSlot = MC.player.getInventory().getStack(i);
            if(stackInSlot.getItem() == item) quantity += stackInSlot.getCount();
        }

        return quantity;
    }

    public static int amountInHotbar(Item item) {
        int quantity = 0;

        for(int i = 0; i <= 9; i++) {
            ItemStack stackInSlot = MC.player.getInventory().getStack(i);
            if(stackInSlot.getItem() == item) quantity += stackInSlot.getCount();
        }
        if(MC.player.getOffHandStack().getItem() == item) quantity += MC.player.getOffHandStack().getCount();

        return quantity;
    }

    public static int amountBlockInHotbar(Block block) {return amountInHotbar(new ItemStack(block).getItem());}

    public static int findItem(Item item) {
        int index = -1;
        for(int i = 0; i < 45; i++) {
            if(MC.player.getInventory().getStack(i).getItem() == item) {
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
            if(MC.player.getInventory().getStack(i).getItem() == item) {
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
                    MC.player.getInventory().getStack(i) == ItemStack.EMPTY
                            || !(MC.player.getInventory().getStack(i).getItem() instanceof BlockItem)
                            || !Block.getBlockFromItem(MC.player.getInventory().getStack(i).getItem()).getDefaultState().isFullCube(MC.world, new BlockPos(0, 0, 0))
            ) continue;

            return i;
        }

        return -1;
    }

    public static int getBlank() {
        int index = -1;
        for(int i = 0; i < 45; i++) {
            if(MC.player.getInventory().getStack(i).isEmpty()) {
                index = i;
                break;
            }
        }
        return index;
    }

    public static int getHotbarBlank() {
        int index = -1;
        for(int i = 0; i < 9; i++) {
            if(MC.player.getInventory().getStack(i).isEmpty()) {
                index = i;
                break;
            }
        }
        return index;
    }

    public static int getInventoryBlank() {
        int index = -1;
        for(int i = 9; i < 45; i++) {
            if(MC.player.getInventory().getStack(i).isEmpty()) {
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
