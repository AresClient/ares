package dev.tigr.ares.core.gui.impl.menu;

import dev.tigr.ares.core.util.render.Color;

import static dev.tigr.ares.core.Ares.FONT_RENDERER;
import static dev.tigr.ares.core.Ares.RENDERER;

/**
 * @author Makrennel 09/28/21
 */
public class MenuSlider extends MenuButton {
    private int sliderPos;
    float val, min, max;

    public MenuSlider(String buttonName) {
        super(buttonName);
    }

    public void setValues(float val, float min, float max) {
        this.val = val;
        this.min = min;
        this.max = max;

        sliderPos = (int)(((this.val - this.min) / (this.max - this.min)) * getWidth());
    }

    public void setVal(float val) {
        this.val = val;

        sliderPos = (int)(((this.val - min) / (max - min)) * getWidth());
    }

    public void setMin(float min) {
        this.min = min;

        sliderPos = (int)(((val - this.min) / (max - this.min)) * getWidth());
    }

    public void setMax(float max) {
        this.max = max;

        sliderPos = (int)(((val - min) / (this.max - min)) * getWidth());
    }

    @Override
    public void draw(int mouseX, int mouseY, float partialTicks) {
        String drawn = buttonName;

        double w = FONT_RENDERER.getStringWidth(drawn, getHeight() /2D);
        double x = getRenderX() + (getWidth() / 2D) - (w / 2);

        RENDERER.drawRect(getRenderX(), getRenderY(), sliderPos, getHeight(), Color.ARES_RED);

        RENDERER.drawLineLoop(2, Color.ARES_RED,
                getRenderX(), getRenderY(),
                getRenderX() + getWidth(), getRenderY(),
                getRenderX() + getWidth(), getRenderY() + getHeight(),
                getRenderX(), getRenderY() + getHeight()
        );

        FONT_RENDERER.drawStringWithCustomHeight(drawn, x, getRenderY() + (getHeight() / 5D), isPressed() ? Color.ARES_RED : Color.WHITE, getHeight() /2D);
    }
}