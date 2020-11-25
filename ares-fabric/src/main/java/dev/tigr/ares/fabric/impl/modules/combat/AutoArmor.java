package dev.tigr.ares.fabric.impl.modules.combat;

import dev.tigr.ares.core.feature.module.Category;
import dev.tigr.ares.core.feature.module.Module;
import dev.tigr.ares.fabric.utils.InventoryUtils;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.screen.slot.SlotActionType;

/**
 * @author Tigermouthbear
 */
@Module.Info(name = "AutoArmor", description = "Automatically equips best armor", category = Category.COMBAT)
public class AutoArmor extends Module {
    @Override
    public void onTick() {
        for(int i = 3; i >= 0; i--) {
            if(MC.player.inventory.armor.get(i).isEmpty()) {
                equipArmor(i);
                break;
            }
        }
    }

    private void equipArmor(int slot) {
        ArmorType armorType = getArmorTypeFromSlot(slot);
        int bestSlot = -1;
        int bestRating = -1;

        for(int i = 9; i <= 44; i++) {
            Item item = MC.player.inventory.getStack(i).getItem();
            if(item instanceof ArmorItem && getArmorTypeFromItem(item) == armorType) {
                int damageReduction = ((ArmorItem) item).getProtection();
                if(damageReduction >= bestRating) {
                    bestSlot = i;
                    bestRating = damageReduction;
                }
            }
        }

        if(bestSlot != -1 && bestRating != -1) MC.interactionManager.clickSlot(0, InventoryUtils.getSlotIndex(bestSlot), 0, SlotActionType.QUICK_MOVE, MC.player);
    }

    private ArmorType getArmorTypeFromItem(Item item) {
        if(Items.NETHERITE_HELMET.equals(item) || Items.DIAMOND_HELMET.equals(item) || Items.GOLDEN_HELMET.equals(item) || Items.IRON_HELMET.equals(item) || Items.CHAINMAIL_HELMET.equals(item) || Items.LEATHER_HELMET.equals(item)) {
            return ArmorType.HELMET;
        } else if(Items.NETHERITE_CHESTPLATE.equals(item) || Items.DIAMOND_CHESTPLATE.equals(item) || Items.GOLDEN_CHESTPLATE.equals(item) || Items.IRON_CHESTPLATE.equals(item) || Items.CHAINMAIL_CHESTPLATE.equals(item) || Items.LEATHER_CHESTPLATE.equals(item)) {
            return ArmorType.CHESTPLATE;
        } else if(Items.NETHERITE_LEGGINGS.equals(item) || Items.DIAMOND_LEGGINGS.equals(item) || Items.GOLDEN_LEGGINGS.equals(item) || Items.IRON_LEGGINGS.equals(item) || Items.CHAINMAIL_LEGGINGS.equals(item) || Items.LEATHER_LEGGINGS.equals(item)) {
            return ArmorType.PANTS;
        } else if(Items.NETHERITE_BOOTS.equals(item) || Items.DIAMOND_BOOTS.equals(item) || Items.GOLDEN_BOOTS.equals(item) || Items.IRON_BOOTS.equals(item) || Items.CHAINMAIL_BOOTS.equals(item) || Items.LEATHER_BOOTS.equals(item)) {
            return ArmorType.BOOTS;
        }
        return null;
    }

    private ArmorType getArmorTypeFromSlot(int slot) {
        switch(slot) {
            case 3:
                return ArmorType.HELMET;
            case 2:
                return ArmorType.CHESTPLATE;
            case 1:
                return ArmorType.PANTS;
            case 0:
                return ArmorType.BOOTS;
            default:
                return null; // shouldn't happen
        }
    }

    enum ArmorType {BOOTS, PANTS, CHESTPLATE, HELMET}
}
