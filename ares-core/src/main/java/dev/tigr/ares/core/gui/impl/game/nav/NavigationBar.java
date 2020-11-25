package dev.tigr.ares.core.gui.impl.game.nav;

import dev.tigr.ares.core.Ares;
import dev.tigr.ares.core.gui.api.Element;
import dev.tigr.ares.core.gui.api.GUI;
import dev.tigr.ares.core.util.function.DynamicValue;
import dev.tigr.ares.core.util.render.Color;
import dev.tigr.ares.core.util.render.LocationIdentifier;

import java.util.ArrayList;
import java.util.List;

import static dev.tigr.ares.core.Ares.FONT_RENDERER;
import static dev.tigr.ares.core.Ares.RENDERER;

/**
 * @author Tigermouthbear 6/16/20
 */
public class NavigationBar extends Element {
    private static final LocationIdentifier HELMET = new LocationIdentifier("textures/logo/ares_helmet_white.png");

    private final List<NavigationButton> navigationButtons = new ArrayList<>();
    private final DynamicValue<Color> color;

    public NavigationBar(GUI gui, DynamicValue<Color> color) {
        super(gui);

        this.color = color;

        setWidth(this::getScreenWidth);
        setHeight(() -> getScreenHeight() / 15);
    }

    public void addNavigationButton(NavigationButton navigationButton) {
        // set width and height
        navigationButton.setHeight(this::getHeight);
        navigationButton.setWidth(this::getHeight);

        // add button to lists
        add(navigationButton);
        navigationButtons.add(navigationButton);

        // set dynamic X for every navigation button
        for(int i = 0; i < navigationButtons.size(); i++) {
            int finalI = i;
            navigationButtons.get(i).setX(() -> getScreenWidth() / 2 - (navigationButtons.size() / 2 * getHeight()) + finalI * getHeight());
        }
    }

    @Override
    public void draw(int mouseX, int mouseY, float partialTicks) {
        // draw background of top bar
        RENDERER.drawRect(getRenderX(), getRenderY(), getWidth(), getHeight(), Color.BLACK);

        // bottom line
        RENDERER.drawLine(
                getRenderX(), getRenderY() + getHeight(),
                getRenderX() + getWidth(), getRenderY() + getHeight(),
                2,
                color.getValue()
        );

        // navigation buttons border lines
        int height = (int) ((getHeight() - (getHeight() * 2 / 3)) / 2);
        int width = (int) (navigationButtons.size() * getHeight());
        int x = (int) (getScreenWidth() / 2 - (navigationButtons.size() / 2 * getHeight()));

        // left
        RENDERER.drawLine(
                x - getHeight() / 4d, getRenderY() + height,
                x - getHeight() / 4d, getRenderY() + getHeight() - height,
                2,
                color.getValue()
        );

        // right
        RENDERER.drawLine(
                x + width + getHeight() / 4d, getRenderY() + height,
                x + width + getHeight() / 4d, getRenderY() + getHeight() - height,
                2,
                color.getValue()
        );

        // draw ares helmet
        RENDERER.drawImage(getRenderX(), getRenderY() + getHeight() / 20d, getHeight() * 0.9d, getHeight() * 0.9d, HELMET, color.getValue());

        // draw ares text
        double textX = FONT_RENDERER.drawStringWithCustomHeightWithShadow("Ares", getHeight() * 0.9d, 0, Color.WHITE, getHeight()) + getHeight() * 0.9d;
        FONT_RENDERER.drawStringWithCustomHeightWithShadow("v" + Ares.VERSION_FULL, (float) textX, 0, Color.WHITE, getHeight() / 3d);

        // draw child elements
        super.draw(mouseX, mouseY, partialTicks);
    }
}
