package dev.tigr.ares.fabric.utils;

import dev.tigr.ares.Wrapper;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.EntityGroup;
import net.minecraft.item.*;
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

    public static int getWeapon() {
        int index = -1;
        double best = 0;
        for(int i = 0; i < 9; i++) {
            ItemStack stack = MC.player.getInventory().getStack(i);
            if(stack.isEmpty()) continue;
            double damage = -1;
            Item item = stack.getItem();
            if(item instanceof MiningToolItem)
                damage = ((MiningToolItem) item).getAttackDamage() + (double) EnchantmentHelper.getAttackDamage(stack, EntityGroup.DEFAULT);
            if(item instanceof SwordItem)
                damage = ((SwordItem) item).getAttackDamage() + (double) EnchantmentHelper.getAttackDamage(stack, EntityGroup.DEFAULT);
            if(damage > best) {
                index = i;
                best = damage;
            }
        }
        return index;
    }

    public static int getTool(BlockPos pos) {
        int index = -1;
        double best = 0;
        for(int i = 0; i < 9; i++) {
            ItemStack stack = MC.player.getInventory().getStack(i);
            if(stack.isEmpty()) continue;

            float speed = stack.getMiningSpeedMultiplier(MC.world.getBlockState(pos));
            if(speed <= 1) continue;

            int efficiency = EnchantmentHelper.getLevel(Enchantments.EFFICIENCY, stack);
            if(efficiency > 0) speed += Math.pow(efficiency, 2) + 1;

            if(speed > best) {
                index = i;
                best = speed;
            }
        }
        return index;
    }

    public static boolean canHarvestWithItemInSlot(BlockState state, int slot) {
        return !state.isToolRequired() || MC.player.getInventory().getStack(slot).isSuitableFor(state);
    }
}
