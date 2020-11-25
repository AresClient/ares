package dev.tigr.ares.forge.impl.modules.hud.elements;

import dev.tigr.ares.core.Ares;
import dev.tigr.ares.core.feature.module.Category;
import dev.tigr.ares.core.feature.module.Module;
import dev.tigr.ares.core.setting.Setting;
import dev.tigr.ares.core.setting.settings.BooleanSetting;
import dev.tigr.ares.core.util.render.Color;
import dev.tigr.ares.core.util.render.IRenderer;
import dev.tigr.ares.core.util.render.LocationIdentifier;
import dev.tigr.ares.forge.impl.modules.hud.HudElement;

/**
 * @author Tigermouthbear
 */
@Module.Info(name = "Watermark", description = "Shows an Ares logo on the hud overlay", category = Category.HUD, enabled = true, visible = false)
public class Watermark extends HudElement {
    private static final LocationIdentifier ARES_LOGO = new LocationIdentifier("textures/logo/ares_logo_white.png");
    private static final int WIDTH = 48;

    private final Setting<Boolean> logo = register(new BooleanSetting("Logo", true));
    private final Setting<Boolean> versionNumber = register(new BooleanSetting("Version Number", true));
    private final Setting<Boolean> rainbow = register(new BooleanSetting("Rainbow", false));

    public Watermark() {
        super(0, 0, WIDTH, WIDTH);
    }

    public void draw() {
        if(logo.getValue() && versionNumber.getValue()) {
            RENDERER.drawImage(getX(), getY(), WIDTH, getHeight(), ARES_LOGO, getHelmetColor());

            String text = "Ares " + Ares.VERSION_FULL;
            drawString(text, getX() + WIDTH, getY() + getHeight() / 2d - FONT_RENDERER.getFontHeight() / 2d, getColor());

            setWidth((int) (WIDTH + 4 + FONT_RENDERER.getStringWidth(text)));
            setHeight(WIDTH);
        } else if(logo.getValue()) {
            RENDERER.drawImage(getX(), getY(), WIDTH, getHeight(), ARES_LOGO, getHelmetColor());

            setWidth(WIDTH);
            setHeight(WIDTH);
        } else if(versionNumber.getValue()) {
            String text = "Ares " + Ares.VERSION_FULL;
            drawString(text, getX(), getY() + getHeight() / 2d - FONT_RENDERER.getFontHeight() / 2d, getColor());

            setWidth((int) FONT_RENDERER.getStringWidth(text));
            setHeight(FONT_RENDERER.getFontHeight());
        }
    }

    private Color getColor() {
        return rainbow.getValue() ? IRenderer.rainbow() : Color.WHITE;
    }

    private Color getHelmetColor() {
        return rainbow.getValue() ? IRenderer.rainbow() : Color.ARES_RED;
    }
}
