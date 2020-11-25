package dev.tigr.ares.forge.impl.modules.hud.elements;

import dev.tigr.ares.core.feature.module.Category;
import dev.tigr.ares.core.feature.module.Module;
import dev.tigr.ares.forge.impl.modules.hud.HudElement;
import dev.tigr.ares.forge.utils.RenderUtils;

/**
 * @author Tigermouthbear
 */
@Module.Info(name = "Armor", description = "Displays the armor you are wearing", category = Category.HUD)
public class Armor extends HudElement {
    private static final int itemSize = 17;

    public Armor() {
        super(320, 1, (itemSize + 2) * 4, itemSize);
    }

    public void draw() {
        int x = getX();

        for(int i = 3; i >= 0; i--) {
            RenderUtils.renderItem(MC.player.inventory.armorInventory.get(i), x, getY());
            x += itemSize + 2;
        }
    }
}
