package dev.tigr.ares.forge.impl.modules.hud.elements;

import dev.tigr.ares.core.feature.module.Category;
import dev.tigr.ares.core.feature.module.Module;
import dev.tigr.ares.core.setting.Setting;
import dev.tigr.ares.core.setting.settings.BooleanSetting;
import dev.tigr.ares.core.util.render.Color;
import dev.tigr.ares.core.util.render.IRenderer;
import dev.tigr.ares.forge.impl.modules.hud.HudElement;
import net.minecraft.tileentity.TileEntityChest;

/**
 * @author Tigermouthbear
 */
@Module.Info(name = "ChestCount", description = "Lists the amount of chests in your render distance", category = Category.HUD)
public class ChestCount extends HudElement {
    private final Setting<Boolean> rainbow = register(new BooleanSetting("Rainbow", false));

    public ChestCount() {
        super(100, 60, 0, FONT_RENDERER.getFontHeight());
    }

    public void draw() {
        int chests = (int) MC.world.loadedTileEntityList.stream().filter(tileEntity -> tileEntity instanceof TileEntityChest).count();
        String text = chests + " chests";
        drawString(text, getX(), getY(), rainbow.getValue() ? IRenderer.rainbow() : Color.WHITE);
        setWidth((int) FONT_RENDERER.getStringWidth(text) + 1);
    }
}
