package dev.tigr.ares.forge.impl.modules.hud.elements;

import dev.tigr.ares.core.feature.module.Category;
import dev.tigr.ares.core.feature.module.Module;
import dev.tigr.ares.forge.impl.modules.hud.EditHudGui;
import dev.tigr.ares.forge.impl.modules.hud.HudElement;
import dev.tigr.ares.forge.utils.RenderUtils;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import org.lwjgl.opengl.GL11;

/**
 * @author Tigermouthbear
 */
@Module.Info(name = "InvPreview", description = "Displays a preview of your inventory on the hud", category = Category.HUD)
public class InvPreview extends HudElement {
    private static final int WIDTH = 168;
    private static final int HEIGHT = 56;

    public InvPreview() {
        super(100, 1, WIDTH, HEIGHT);
    }

    public void draw() {
        GL11.glPushMatrix();
        if(!(MC.currentScreen instanceof EditHudGui) && background.getValue() == Background.NONE) renderBackground();
        renderItems();
        GL11.glPopMatrix();
    }

    private void renderBackground() {
        Gui.drawRect(getX(), getY(), getX() + getWidth(), getY() + getHeight(), GRAY.getRGB());
        GlStateManager.enableTexture2D();
    }

    private void renderItems() {
        NonNullList<ItemStack> items = MC.player.inventory.mainInventory;

        int x = getX() + 4;
        int y = getY() - HEIGHT / 3 + 2;

        for(int i = 9; i < items.size(); i++) {
            if(i % 9 == 0) {
                x = getX() + 4;
                y += HEIGHT / 3;
            }

            RenderUtils.renderItem(items.get(i), x, y);

            x += WIDTH / 9;
        }
    }
}
