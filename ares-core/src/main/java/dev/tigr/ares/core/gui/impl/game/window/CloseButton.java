package dev.tigr.ares.core.gui.impl.game.window;

import dev.tigr.ares.core.gui.api.Element;
import dev.tigr.ares.core.gui.api.GUI;
import dev.tigr.ares.core.gui.impl.game.ClickGUI;
import dev.tigr.ares.core.util.render.Color;

import static dev.tigr.ares.core.Ares.RENDERER;

/**
 * @author Tigermouthbear 6/18/20
 */
class CloseButton extends Element implements ClickGUI.Globals {
    private final Window element;

    public CloseButton(GUI gui, Window element) {
        super(gui);

        this.element = element;
    }

    @Override
    public void draw(int mouseX, int mouseY, float partialTicks) {
        // draw hovering background
        if(isMouseOver(mouseX, mouseY))
            RENDERER.drawImage(getRenderX(), getRenderY(), getWidth(), getHeight(), HOVER_BACKGROUND, HOVER_BACKGROUND_COLOR);

        // calculate position of X
        double length = Math.sqrt(getHeight() * 2 + getHeight() * 2) / 2d;
        double x = getRenderX() + getWidth() / 2d;
        double y = getRenderY() + getHeight() / 2d;

        // draw X
        RENDERER.drawLine(x - length, y + length, x + length, y - length, 2, Color.WHITE);
        RENDERER.drawLine(x + length, y + length, x - length, y - length, 2, Color.WHITE);

        // draw child elements
        super.draw(mouseX, mouseY, partialTicks);
    }

    @Override
    public void click(int mouseX, int mouseY, int mouseButton) {
        // toggle visibility of element on click
        if(isMouseOver(mouseX, mouseY) && mouseButton == 0) {
            element.toggleVisibility();
            element.setDragging(false);
        }

        // call click method for child elements
        super.click(mouseX, mouseY, mouseButton);
    }
}