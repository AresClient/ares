package dev.tigr.ares.core.gui.impl.menu;

import dev.tigr.ares.core.gui.api.Element;
import dev.tigr.ares.core.gui.api.GUI;
import dev.tigr.ares.core.util.function.Hook;
import dev.tigr.ares.core.util.render.Color;

import static dev.tigr.ares.core.Ares.FONT_RENDERER;
import static dev.tigr.ares.core.Ares.RENDERER;

/**
 * @author Tigermouthbear & Makrennel 11/30/21
 */
public class MenuButton extends Element {
    private final String text;
    private final Hook onClick;

    private boolean pressed = false;

    public MenuButton(GUI gui, String text, Hook onClick) {
        super(gui);
        this.text = text;
        this.onClick = onClick;

        setHeight(() -> getScreenHeight() / 20D);
        setWidth(() -> FONT_RENDERER.getStringWidth(text, getHeight()));
    }

    @Override
    public void click(int mouseX, int mouseY, int mouseButton) {
        if(mouseButton == 0 && isMouseOver(mouseX, mouseY)) {
            pressed = true;
        }
        super.click(mouseX, mouseY, mouseButton);
    }

    @Override
    public void release(int mouseX, int mouseY, int mouseButton) {
        if(mouseButton == 0) {
            if(pressed && isMouseOver(mouseX, mouseY)) {
                onClick.invoke();
            }
            pressed = false;
        }
        super.release(mouseX, mouseY, mouseButton);
    }

    @Override
    public void draw(int mouseX, int mouseY, float partialTicks) {
        RENDERER.drawRect(getRenderX(), getRenderY(), getWidth(), getHeight(), Color.BLACK);

        RENDERER.drawLineLoop(2, Color.ARES_RED,
                getRenderX(), getRenderY(),
                getRenderX() + getWidth(), getRenderY(),
                getRenderX() + getWidth(), getRenderY() + getHeight(),
                getRenderX(), getRenderY() + getHeight()
        );

        double height = getHeight() / 3D * 2D;
        double width = FONT_RENDERER.getStringWidth(text, height);
        FONT_RENDERER.drawStringWithCustomHeight(text, getRenderX() + getWidth() / 2 - width / 2, getRenderY() + height / 4D, pressed ? Color.ARES_RED : Color.WHITE, height);

        super.draw(mouseX, mouseY, partialTicks);
    }
}
