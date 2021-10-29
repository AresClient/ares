package dev.tigr.ares.forge.utils;

import dev.tigr.ares.core.util.global.ReflectionHelper;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.EnumCreatureAttribute;
import net.minecraft.init.Enchantments;
import net.minecraft.item.*;
import net.minecraft.util.math.BlockPos;

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

    public static int getHotbarBlank() {
        int index = -1;
        for(int i = 0; i < 9; i++) {
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

    public static int getWeapon() {
        int index = -1;
        double best = 0;
        for(int i = 0; i < 9; i++) {
            ItemStack stack = MC.player.inventory.getStackInSlot(i);
            if(stack.isEmpty()) continue;
            double damage = -1;
            Item item = stack.getItem();
            if(item instanceof ItemTool)
                damage = (float) (ReflectionHelper.getPrivateValue(ItemTool.class, (ItemTool) item, "attackDamage", "field_77865_bY")) + (double) EnchantmentHelper.getModifierForCreature(stack, EnumCreatureAttribute.UNDEFINED);
            if(item instanceof ItemSword)
                damage = ((ItemSword) item).getAttackDamage() + (double) EnchantmentHelper.getModifierForCreature(stack, EnumCreatureAttribute.UNDEFINED);
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
            ItemStack stack = MC.player.inventory.getStackInSlot(i);
            if(stack.isEmpty()) continue;

            float speed = stack.getDestroySpeed(MC.world.getBlockState(pos));
            if(speed <= 1) continue;

            int efficiency = EnchantmentHelper.getEnchantmentLevel(Enchantments.EFFICIENCY, stack);
            if(efficiency > 0) speed += Math.pow(efficiency, 2) + 1;

            if(speed > best) {
                index = i;
                best = speed;
            }
        }
        return index;
    }

    public static boolean canHarvestWithItemInSlot(IBlockState state, int slot) {
        String tool = state.getBlock().getHarvestTool(state);
        if(tool == null) return false;
        return MC.player.inventory.getStackInSlot(slot).getItem().getHarvestLevel(MC.player.inventory.getStackInSlot(slot), tool, null, null) >= state.getBlock().getHarvestLevel(state);
    }
}
