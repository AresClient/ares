package dev.tigr.ares.fabric.impl.modules.hud.elements;

import dev.tigr.ares.core.feature.module.Category;
import dev.tigr.ares.core.feature.module.Module;
import dev.tigr.ares.fabric.impl.modules.hud.HudElement;
import dev.tigr.ares.fabric.utils.InventoryUtils;
import dev.tigr.ares.fabric.utils.render.RenderUtils;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;

/**
 * @author Tigermouthbear
 * updated to 1.16.2 on 8/28/20
 */
@Module.Info(name = "CrystalCount", description = "Displays how many crystals are in your inventory", category = Category.HUD)
public class CrystalCount extends HudElement {
    public CrystalCount() {
        super(290, 1, 18, 18);
    }

    public void draw() {
        ItemStack crystalStack = new ItemStack(Items.END_CRYSTAL);
        crystalStack.setCount(InventoryUtils.amountInInventory(Items.END_CRYSTAL));

        RenderUtils.renderItemStack(crystalStack, getX(), getY());
    }
}
