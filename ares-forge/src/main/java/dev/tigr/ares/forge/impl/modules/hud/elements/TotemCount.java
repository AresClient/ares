package dev.tigr.ares.forge.impl.modules.hud.elements;

import dev.tigr.ares.core.feature.module.Category;
import dev.tigr.ares.core.feature.module.Module;
import dev.tigr.ares.forge.impl.modules.hud.HudElement;
import dev.tigr.ares.forge.utils.InventoryUtils;
import dev.tigr.ares.forge.utils.render.RenderUtils;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;

/**
 * @author Tigermouthbear
 */
@Module.Info(name = "TotemCount", description = "Displays how many totems are in your inventory", category = Category.HUD)
public class TotemCount extends HudElement {
    public TotemCount() {
        super(270, 1, 18, 18);
    }

    public void draw() {
        ItemStack totemStack = new ItemStack(Items.TOTEM_OF_UNDYING);
        totemStack.setCount(InventoryUtils.amountInInventory(Items.TOTEM_OF_UNDYING));

        RenderUtils.renderItem(totemStack, getX(), getY());
    }
}
