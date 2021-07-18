package dev.tigr.ares.core.gui.impl.game.window.windows.modules;

import dev.tigr.ares.core.feature.module.Category;
import dev.tigr.ares.core.gui.api.Element;
import dev.tigr.ares.core.gui.api.GUI;
import dev.tigr.ares.core.gui.impl.game.window.OpenCloseTimer;
import dev.tigr.ares.core.setting.Setting;
import dev.tigr.ares.core.setting.SettingCategory;
import dev.tigr.ares.core.setting.settings.BooleanSetting;
import dev.tigr.ares.core.setting.settings.numerical.DoubleSetting;
import dev.tigr.ares.core.util.function.DynamicValue;
import dev.tigr.ares.core.util.global.Utils;
import dev.tigr.ares.core.util.render.Color;

import static dev.tigr.ares.core.Ares.FONT_RENDERER;
import static dev.tigr.ares.core.Ares.RENDERER;

/**
 * @author Tigermouthbear 12/30/20
 */
public class ExpandedCategoryElement extends Element {
    private final DynamicValue<Color> color;
    private final Category category;
    private final Setting<Double> x;
    private final Setting<Double> y;
    private final Setting<Boolean> open;
    private final OpenCloseTimer openCloseTimer = new OpenCloseTimer(300, false);
    public boolean dragging = false;
    private double diffX = 0;
    private double diffY = 0;

    public ExpandedCategoryElement(GUI gui, DynamicValue<Color> color, Category category, SettingCategory parentSettingCategory, double defaultX, double defaultY) {
        super(gui);

        this.color = color;
        this.category = category;

        // create settings
        SettingCategory settingCategory = new SettingCategory(parentSettingCategory, category.name());
        x = new DoubleSetting(settingCategory, "x", defaultX, 0, 1);
        y = new DoubleSetting(settingCategory, "y", defaultY, 0, 1);
        open = new BooleanSetting(settingCategory, "open", false);

        // sync open timer with setting
        openCloseTimer.setStateHard(open.getValue());

        // set default position
        setX(() -> x.getValue() * getScreenWidth());
        setY(() -> y.getValue() * getScreenHeight());

        // add category element
        CategoryElement categoryElement = new CategoryElement(getGUI(), category, color, () -> getExpandedHeight() / 18d, () -> Math.max(getHeight() - getTopHeight(), 0), 1);
        categoryElement.setY(this::getTopHeight);
        categoryElement.setWidth(this::getWidth);
        categoryElement.setHeight(() -> getHeight() - getTopHeight());
        add(categoryElement);

        // set size
        setWidth(() -> getScreenWidth() / 8.4d);
        setHeight(() -> Math.min(getTopHeight() + openCloseTimer.getAnimationFactor() * getExpandedHeight(), getTopHeight() + categoryElement.getHeight(categoryElement.getColumn(0))));
    }

    @Override
    public void draw(int mouseX, int mouseY, float partialTicks) {
        // update position based on drag and clamp to keep in bounds
        if(dragging) {
            x.setValue((Utils.clamp(mouseX - diffX, 0, getScreenWidth() - getWidth())) / getScreenWidth());
            y.setValue((Utils.clamp(mouseY - diffY, getScreenHeight() / 15, getScreenHeight() - getHeight())) / getScreenHeight());
        }

        // tick timer
        openCloseTimer.tick();

        // draw backdrop
        RENDERER.drawRect(getRenderX(), getRenderY(), getWidth(), getHeight(), Color.BLACK);

        // draw outline
        RENDERER.drawLineLoop(1, color.getValue(),
                getRenderX(), getRenderY(),
                getRenderX() + getWidth(), getRenderY(),
                getRenderX() + getWidth(), getRenderY() + getHeight(),
                getRenderX(), getRenderY() + getHeight()
        );
        RENDERER.drawLine(getRenderX(), getRenderY() + getTopHeight(), getRenderX() + getWidth(), getRenderY() + getTopHeight(), 1, color.getValue());

        // draw category name
        double width = FONT_RENDERER.getStringWidth(category.name(), getTopHeight());
        FONT_RENDERER.drawStringWithCustomHeight(category.name(), getRenderX() + getWidth() / 2 - width / 2, getRenderY(), Color.WHITE, getTopHeight());

        // draw child elements if not collapsed
        if(openCloseTimer.getAnimationFactor() != 0) super.draw(mouseX, mouseY, partialTicks);
    }

    @Override
    public void click(int mouseX, int mouseY, int mouseButton) {
        // if mouse is clicked over bar, start dragging or hide category
        if(isMouseOver(mouseX, mouseY)) {
            if(mouseY <= getRenderY() + getTopHeight()) {
                if(mouseButton == 0) {
                    dragging = true;
                    diffX = mouseX - getRenderX();
                    diffY = mouseY - getRenderY();
                } else if(mouseButton == 1) toggleVisibility();
            } else if(open.getValue()) super.click(mouseX, mouseY, mouseButton);
        }
    }

    @Override
    public void scroll(double mouseX, double mouseY, double value) {
        // only scroll when category is open
        if(open.getValue()) super.scroll(mouseX, mouseY, value);
    }

    @Override
    public void release(int mouseX, int mouseY, int mouseButton) {
        // stop dragging on release
        if(mouseButton == 0) dragging = false;

        super.release(mouseX, mouseY, mouseButton);
    }

    public double getTopHeight() {
        return getScreenHeight() / 30d;
    }

    public double getExpandedHeight() {
        return getScreenHeight() / 1.6d;
    }

    @Override
    public void toggleVisibility() {
        open.setValue(!open.getValue());
        openCloseTimer.setState(open.getValue());
    }
}
