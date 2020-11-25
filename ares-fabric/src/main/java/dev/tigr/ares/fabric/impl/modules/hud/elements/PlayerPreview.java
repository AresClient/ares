package dev.tigr.ares.fabric.impl.modules.hud.elements;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.tigr.ares.core.feature.module.Category;
import dev.tigr.ares.core.feature.module.Module;
import dev.tigr.ares.fabric.impl.modules.hud.HudElement;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;

/**
 * @author Tigermouthbear
 * updated to 1.16.2 on 8/28/20
 */
@Module.Info(name = "PlayerPreview", description = "Shows a preview of what your player looks like on the hud", category = Category.HUD)
public class PlayerPreview extends HudElement {
    public PlayerPreview() {
        super(300, 100, 25, 25);
        background.setVisibility(() -> false);
    }

    public void draw() {
        InventoryScreen.drawEntity(getX(), getY(), 40, 0, 0, MC.player);
        RenderSystem.enableAlphaTest();
    }
}
