package dev.tigr.ares.core.gui.impl.game.window.windows;

import dev.tigr.ares.core.gui.api.GUI;
import dev.tigr.ares.core.gui.impl.game.window.Window;
import dev.tigr.ares.core.util.function.DynamicValue;
import dev.tigr.ares.core.util.render.Color;
import dev.tigr.ares.core.util.render.LocationIdentifier;

import static dev.tigr.ares.core.Ares.*;

/**
 * @author Tigermouthbear 6/18/20
 */
public class WelcomeWindow extends Window {
    private static final LocationIdentifier HELMET = new LocationIdentifier("textures/logo/ares_logo_white.png");

    public WelcomeWindow(GUI gui, DynamicValue<Color> color) {
        super(gui, "Welcome", color, true, 0.4, 0.35);

        setWidth(() -> getScreenWidth() / 5d);
        setHeight(() -> getScreenHeight() / 3d);
    }

    @Override
    public void draw(int mouseX, int mouseY) {
        // draw Ares Logo
        double size = getBarHeight() * 9;
        RENDERER.drawImage(getRenderX() + getWidth() / 2d - size / 2d, getRenderY() + getBarHeight() * 1.5d, size, size, HELMET, Color.ARES_RED);

        // draw welcome text
        String text = "Welcome " + UTILS.getPlayerName() + "!";
        double height = FONT_RENDERER.getFontHeightWithCustomWidth(text, getWidth() - getWidth()/10d);
        FONT_RENDERER.drawStringWithCustomWidthWithShadow(text, getRenderX() + getWidth()/20d, getRenderY() + getHeight() - height/2 - (getHeight() - getBarHeight() * 1.5d - size)/2, Color.WHITE, getWidth() - getWidth()/10d);
    }
}
