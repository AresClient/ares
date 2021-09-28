package dev.tigr.ares.core.gui.impl.menu;

import dev.tigr.ares.core.util.render.Color;
import dev.tigr.ares.core.util.render.LocationIdentifier;
import dev.tigr.ares.core.util.render.TextColor;

import static dev.tigr.ares.core.Ares.FONT_RENDERER;
import static dev.tigr.ares.core.Ares.RENDERER;

/**
 * @author Makrennel 09/28/21
 */
public class MenuButton extends MenuElement {
    private static final LocationIdentifier onSwitch = new LocationIdentifier("textures/onswitch.png");
    private static final LocationIdentifier offSwitch = new LocationIdentifier("textures/offswitch.png");

    protected String buttonName;
    protected boolean pressed;
    protected boolean onOffSwitch = false;

    public MenuButton(MenuElement parent, String buttonName) {
        super(parent);
        this.buttonName = buttonName;
    }

    public MenuButton(String buttonName) {
        this.buttonName = buttonName;
    }

    public void setOnOffSwitch(boolean onOffSwitch) {
        this.onOffSwitch = onOffSwitch;
    }

    public void setButtonName(String name) {
        this.buttonName = name;
    }

    public String getButtonName() {
        return buttonName;
    }

    public boolean isPressed() {
        return pressed;
    }

    public void press() {
        setPressed(!pressed);
    }

    public void setPressed(boolean pressed) {
        this.pressed = pressed;
    }

    public void draw(int mouseX, int mouseY, float partialTicks) {
        if(onOffSwitch) {
            if(pressed) RENDERER.drawImage(getRenderX(), getRenderY(), getWidth(), getHeight(), onSwitch);
            else RENDERER.drawImage(getRenderX(), getRenderY(), getWidth(), getHeight(), offSwitch);
        } else {
            if (isMouseOver(mouseX, mouseY) && !isPressed())
                RENDERER.drawRect(getRenderX(), getRenderY(), getWidth(), getHeight(), new Color(0.5F, 0.5F, 0.5F, 0.2F));

            double w = FONT_RENDERER.getStringWidth(buttonName, getHeight() /2D);
            double l = w;
            if(w > getWidth()) w = 11 *(getWidth() /16D);
            double x = getRenderX() + (getWidth() / 2D) - (w / 2);

            RENDERER.drawLineLoop(2, Color.ARES_RED,
                    getRenderX(), getRenderY(),
                    getRenderX() + getWidth(), getRenderY(),
                    getRenderX() + getWidth(), getRenderY() + getHeight(),
                    getRenderX(), getRenderY() + getHeight()
            );

            // Draw string, ensuring no overflow
            if(l > getWidth()) FONT_RENDERER.drawStringWithCustomWidth(buttonName, x, getRenderY() + (getHeight() / 5D), isPressed() ? Color.ARES_RED : Color.WHITE, 11 *(getWidth() /12D));
            else FONT_RENDERER.drawStringWithCustomHeight(buttonName, x, getRenderY() + (getHeight() / 5D), isPressed() ? Color.ARES_RED : Color.WHITE, getHeight() /2D);
        }
    }

    public boolean isMouseOver(double mouseX, double mouseY) {
        if(onOffSwitch) {
            return mouseX >= getRenderX() + (getWidth() *0.0976)
                    && mouseX <= getRenderX() + getWidth() - (getWidth() *0.0976)
                    && mouseY >= getRenderY() + (getHeight() *0.34)
                    && mouseY <= getRenderY() + getHeight() - (getHeight() *0.34);
        }
        return mouseX >= getRenderX()
                && mouseX <= getRenderX() + getWidth()
                && mouseY >= getRenderY()
                && mouseY <= getRenderY() + getHeight();
    }

    public void onClick(double mouseX, double mouseY) {
        if(isMouseOver(mouseX, mouseY)) press();
    }
}
