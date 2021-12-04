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
@Module.Info(name = "TotemCount", description = "Displays how many totems are in your inventory", category = Category.HUD)
public class TotemCount extends HudElement {
    public TotemCount() {
        super(270, 1, 18, 18);
    }

    public void draw() {
        ItemStack totemStack = new ItemStack(Items.TOTEM_OF_UNDYING);
        totemStack.setCount(InventoryUtils.amountInInventory(Items.TOTEM_OF_UNDYING));

        RenderUtils.renderItemStack(totemStack, getX(), getY());
    }
}
