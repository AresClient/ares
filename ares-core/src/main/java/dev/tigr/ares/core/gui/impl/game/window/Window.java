package dev.tigr.ares.core.gui.impl.game.window;

import dev.tigr.ares.core.gui.api.Element;
import dev.tigr.ares.core.gui.api.GUI;
import dev.tigr.ares.core.gui.impl.game.ClickGUI;
import dev.tigr.ares.core.setting.Setting;
import dev.tigr.ares.core.setting.SettingCategory;
import dev.tigr.ares.core.setting.settings.BooleanSetting;
import dev.tigr.ares.core.setting.settings.numerical.DoubleSetting;
import dev.tigr.ares.core.util.function.DynamicValue;
import dev.tigr.ares.core.util.global.Utils;
import dev.tigr.ares.core.util.render.Color;
import dev.tigr.ares.core.util.render.LocationIdentifier;

import static dev.tigr.ares.core.Ares.*;

/**
 * Window on the ares gui with open and close animations and capability
 *
 * @author Tigermouthbear 6/18/20
 */
public class Window extends Element {
    protected static final Color BAR_COLOR = new Color(0.15f, 0.15f, 0.15f, 0.9f);
    protected static final Color BACKGROUND_COLOR = Color.BLACK;
    protected static final LocationIdentifier CIRCLE = new LocationIdentifier("textures/circle.png");
    protected final DynamicValue<Color> color;
    protected final SettingCategory settingCategory;
    protected final Setting<Double> x;
    protected final Setting<Double> y;
    protected final Setting<Boolean> open;
    protected final OpenCloseTimer openCloseTimer = new OpenCloseTimer(200, true);
    protected final String name;
    protected boolean dragging = false;
    protected double diffX = 0;
    protected double diffY = 0;

    public Window(GUI gui, String name, DynamicValue<Color> color, boolean defaultOpen, double defaultX, double defaultY) {
        super(gui);

        // set name and color
        this.name = name;
        this.color = color;

        // create settings
        settingCategory = new SettingCategory(ClickGUI.SETTING_CATEGORY, name);
        x = new DoubleSetting(settingCategory, "x", defaultX, 0, 1);
        y = new DoubleSetting(settingCategory, "y", defaultY, 0, 1);

        // create open setting and sync animations
        open = new BooleanSetting(settingCategory, "open", defaultOpen);
        openCloseTimer.setStateHard(open.getValue());

        // set default position
        setVisibility(openCloseTimer::getState);
        setX(() -> x.getValue() * getScreenWidth());
        setY(() -> y.getValue() * getScreenHeight());

        // add close button
        CloseButton closeButton = new CloseButton(getGUI(), this);
        closeButton.setX(() -> getWidth() - getBarHeight());
        closeButton.setWidth(this::getBarHeight);
        closeButton.setHeight(this::getBarHeight);
        add(closeButton);
    }

    public void draw(int mouseX, int mouseY) {
    }

    @Override
    public void draw(int mouseX, int mouseY, float partialTicks) {
        // update position based on drag and clamp to keep in bounds
        if(dragging) {
            x.setValue((Utils.clamp(mouseX - diffX, 0, getScreenWidth() - getWidth())) / getScreenWidth());
            y.setValue((Utils.clamp(mouseY - diffY, getScreenHeight() / 15, getScreenHeight() - getHeight())) / getScreenHeight());
        }

        openCloseTimer.tick();

        // scale for animation
        double animScale = Math.max(openCloseTimer.getAnimationFactor(), 0.01);
        RENDER_STACK.scale(animScale, animScale, 1);

        // draw move section
        RENDERER.drawRect(getRenderX(), getRenderY(), getWidth(), getBarHeight(), BAR_COLOR);

        // draw background
        RENDERER.drawRect(getRenderX(), getRenderY() + getBarHeight(), getWidth(), getHeight() - getBarHeight(), BACKGROUND_COLOR);

        // draw outline for move bar
        RENDERER.drawLineLoop(1, color.getValue(),
                getRenderX(), getRenderY(),
                getRenderX() + getWidth(), getRenderY(),
                getRenderX() + getWidth(), getRenderY() + getHeight(),
                getRenderX(), getRenderY() + getHeight()
        );
        RENDERER.drawLine(getRenderX(), getRenderY() + getBarHeight(), getRenderX() + getWidth(), getRenderY() + getBarHeight(), 1, color.getValue());

        // draw dots
        double size = getBarHeight() / 3;
        double seperation = size * 4 / 3;
        double y = (getBarHeight() - size) / 2;
        double x = getRenderX() + getWidth() / 2;
        x -= 3 * seperation / 2;
        for(int i = 0; i < 3; i++) {
            RENDERER.drawImage(x, getRenderY() + y, size, size, CIRCLE);
            x += seperation;
        }

        // draw window name
        FONT_RENDERER.drawStringWithCustomHeight(name, getRenderX() + 1, getRenderY() - 1, Color.WHITE, getBarHeight());

        // draw child element's
        super.draw(mouseX, mouseY, partialTicks);
        draw(mouseX, mouseY);

        // reset animation scale
        RENDER_STACK.scale(1 / animScale, 1 / animScale, 1);
    }

    @Override
    public void click(int mouseX, int mouseY, int mouseButton) {
        // if mouse is clicked over bar, start dragging
        if(mouseButton == 0 && isMouseOver(mouseX, mouseY) && mouseY <= getRenderY() + getBarHeight()) {
            dragging = true;
            diffX = mouseX - getRenderX();
            diffY = mouseY - getRenderY();
        }

        super.click(mouseX, mouseY, mouseButton);
    }

    @Override
    public void release(int mouseX, int mouseY, int mouseButton) {
        // stop dragging on release
        if(mouseButton == 0) dragging = false;

        super.release(mouseX, mouseY, mouseButton);
    }

    public double getBarHeight() {
        return getScreenHeight() / 40d;
    }

    public void setDragging(boolean value) {
        dragging = value;
    }

    @Override
    public void toggleVisibility() {
        open.setValue(!open.getValue());
        openCloseTimer.setState(open.getValue());
    }
}
