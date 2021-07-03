package dev.tigr.ares.fabric.impl.modules.hud.elements;

import dev.tigr.ares.core.feature.module.Category;
import dev.tigr.ares.core.feature.module.Module;
import dev.tigr.ares.fabric.impl.modules.hud.EditHudGui;
import dev.tigr.ares.fabric.impl.modules.hud.HudElement;
import dev.tigr.ares.fabric.utils.RenderUtils;
import net.minecraft.item.ItemStack;
import net.minecraft.util.collection.DefaultedList;

/**
 * @author Tigermouthbear
 * updated to 1.16.2 on 8/28/20
 */
@Module.Info(name = "InvPreview", description = "Displays a preview of your inventory on the hud", category = Category.HUD)
public class InvPreview extends HudElement {
    private static final int WIDTH = 168;
    private static final int HEIGHT = 56;

    public InvPreview() {
        super(100, 1, WIDTH, HEIGHT);
    }

    public void draw() {
        if(!(MC.currentScreen instanceof EditHudGui) && background.getValue() == Background.NONE) renderBackground();
        renderItems();
    }

    private void renderBackground() {
        RENDERER.drawRect(getX(), getY(), getWidth(), getHeight(), GRAY);
    }

    private void renderItems() {
        DefaultedList<ItemStack> items = MC.player.getInventory().main;

        int x = getX() + 4;
        int y = getY() - HEIGHT / 3 + 2;

        for(int i = 9; i < items.size(); i++) {
            if(i % 9 == 0) {
                x = getX() + 4;
                y += HEIGHT / 3;
            }

            RenderUtils.renderItemStack(items.get(i), x, y);

            x += WIDTH / 9;
        }
    }
}
