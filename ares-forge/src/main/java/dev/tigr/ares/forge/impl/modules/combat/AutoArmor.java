package dev.tigr.ares.forge.impl.modules.combat;

import dev.tigr.ares.core.feature.module.Category;
import dev.tigr.ares.core.feature.module.Module;
import net.minecraft.init.Items;
import net.minecraft.inventory.ClickType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemArmor;

/**
 * @author Tigermouthbear
 */
@Module.Info(name = "AutoArmor", description = "Automatically equips best armor", category = Category.COMBAT)
public class AutoArmor extends Module {
    @Override
    public void onTick() {
        for(int i = 3; i >= 0; i--) {
            if(MC.player.inventory.armorInventory.get(i).isEmpty()) {
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
            Item item = MC.player.inventoryContainer.getSlot(i).getStack().getItem();
            if(item instanceof ItemArmor && getArmorTypeFromItem(item) == armorType) {
                int damageReduction = ((ItemArmor) item).damageReduceAmount;
                if(damageReduction >= bestRating) {
                    bestSlot = i;
                    bestRating = damageReduction;
                }
            }
        }

        if(bestSlot != -1 && bestRating != -1) MC.playerController.windowClick(0, bestSlot, 0, ClickType.QUICK_MOVE, MC.player);
    }

    private ArmorType getArmorTypeFromItem(Item item) {
        if(Items.DIAMOND_HELMET.equals(item) || Items.GOLDEN_HELMET.equals(item) || Items.IRON_HELMET.equals(item) || Items.CHAINMAIL_HELMET.equals(item) || Items.LEATHER_HELMET.equals(item)) {
            return ArmorType.HELMET;
        } else if(Items.DIAMOND_CHESTPLATE.equals(item) || Items.GOLDEN_CHESTPLATE.equals(item) || Items.IRON_CHESTPLATE.equals(item) || Items.CHAINMAIL_CHESTPLATE.equals(item) || Items.LEATHER_CHESTPLATE.equals(item)) {
            return ArmorType.CHESTPLATE;
        } else if(Items.DIAMOND_LEGGINGS.equals(item) || Items.GOLDEN_LEGGINGS.equals(item) || Items.IRON_LEGGINGS.equals(item) || Items.CHAINMAIL_LEGGINGS.equals(item) || Items.LEATHER_LEGGINGS.equals(item)) {
            return ArmorType.PANTS;
        } else if(Items.DIAMOND_BOOTS.equals(item) || Items.GOLDEN_BOOTS.equals(item) || Items.IRON_BOOTS.equals(item) || Items.CHAINMAIL_BOOTS.equals(item) || Items.LEATHER_BOOTS.equals(item)) {
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
