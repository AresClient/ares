package dev.tigr.ares.fabric.impl.modules.hud.elements;

import dev.tigr.ares.core.feature.module.Category;
import dev.tigr.ares.core.feature.module.Module;
import dev.tigr.ares.fabric.impl.modules.hud.HudElement;
import dev.tigr.ares.fabric.utils.RenderUtils;

/**
 * @author Tigermouthbear
 * updated to 1.16.2 on 8/28/20
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
            RenderUtils.renderItemStack(MC.player.inventory.armor.get(i), x, getY());
            x += itemSize + 2;
        }
    }
}
