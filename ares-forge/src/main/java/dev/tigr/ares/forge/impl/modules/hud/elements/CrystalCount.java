package dev.tigr.ares.forge.impl.modules.hud.elements;

import dev.tigr.ares.core.feature.module.Category;
import dev.tigr.ares.core.feature.module.Module;
import dev.tigr.ares.forge.impl.modules.hud.HudElement;
import dev.tigr.ares.forge.utils.InventoryUtils;
import dev.tigr.ares.forge.utils.RenderUtils;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;

/**
 * @author Tigermouthbear
 */
@Module.Info(name = "CrystalCount", description = "Displays how many crystals are in your inventory", category = Category.HUD)
public class CrystalCount extends HudElement {
    public CrystalCount() {
        super(290, 1, 18, 18);
    }

    public void draw() {
        ItemStack totemStack = new ItemStack(Items.END_CRYSTAL);
        totemStack.setCount(InventoryUtils.amountInInventory(Items.END_CRYSTAL));

        RenderUtils.renderItem(totemStack, getX(), getY());
    }
}
