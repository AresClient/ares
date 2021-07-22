package dev.tigr.ares.core.gui.impl.game.window.windows.modules;

import dev.tigr.ares.core.feature.module.Module;
import dev.tigr.ares.core.gui.api.Element;
import dev.tigr.ares.core.gui.api.GUI;
import dev.tigr.ares.core.gui.impl.game.window.OpenCloseTimer;
import dev.tigr.ares.core.gui.impl.game.window.windows.modules.settings.SettingElement;
import dev.tigr.ares.core.setting.Setting;
import dev.tigr.ares.core.setting.settings.ListSetting;
import dev.tigr.ares.core.util.function.DynamicValue;
import dev.tigr.ares.core.util.render.Color;
import dev.tigr.ares.core.util.render.LocationIdentifier;

import static dev.tigr.ares.core.Ares.*;

/**
 * @author Tigermouthbear 6/25/20
 */
public class ModuleElement extends Element {
    private static final LocationIdentifier DROP_DOWN_ARROW = new LocationIdentifier("textures/arrow.png");

    private final Module module;
    private final DynamicValue<Color> color;
    private final DynamicValue<Double> modHeight;
    private final OpenCloseTimer open = new OpenCloseTimer(200, false);
    private DynamicValue<Double> offset;

    public ModuleElement(GUI gui, Module module, DynamicValue<Color> color, DynamicValue<Double> modHeight) {
        super(gui);
        this.module = module;
        this.color = color;
        this.modHeight = modHeight;

        setHeight(() -> {
            long count = 0L;
            for(Setting<?> setting: module.getSettings()) {
                if(setting.isVisible()) {
                    count++;
                }
            }
            return getModHeight() + open.getAnimationFactor() * (getModHeight() / 2d * count);
        });

        SettingElement<?> prev = null;
        for(Setting<?> setting: module.getSettings()) {
            if(setting instanceof ListSetting) continue;
            SettingElement<?> settingElement = SettingElement.create(getGUI(), setting);
            settingElement.setHeight(() -> getModHeight() / 2d);
            settingElement.setWidth(this::getWidth);
            if((settingElement.getRenderY() + settingElement.getHeight()) <= (getRenderY() + getHeight())) settingElement.setVisibility(() -> open.getState() && setting.isVisible());
            if(prev == null) settingElement.setY(this::getModHeight);
            else {
                SettingElement<?> finalPrev = prev;
                settingElement.setY(() -> finalPrev.getY() + (finalPrev.isVisible() ? finalPrev.getHeight() : 0));
            }

            add(settingElement);
            prev = settingElement;
        }
    }

    @Override
    public void draw(int mouseX, int mouseY, float partialTicks) {
        super.draw(mouseX, mouseY, partialTicks);

        // tick animations
        open.tick();

        // set tooltip if hovering
        if(isMouseOver(mouseX, mouseY) && getRenderY() + offset.getValue() + getModHeight() > mouseY)
            getGUI().setTooltip(module.getDescription());

        // draw drop down arrow with rotations based on open status and animation
        RENDER_STACK.push();
        RENDER_STACK.translate(getRenderX() + getModHeight() / 2d, getRenderY() + getModHeight() / 2d, 0);
        RENDER_STACK.rotate(90 * (float) open.getAnimationFactor(), 0, 0, 1);
        RENDER_STACK.translate(-(getRenderX() + getModHeight() / 2d), -(getRenderY() + getModHeight() / 2d), 0);
        RENDERER.drawImage(getRenderX(), getRenderY(), getModHeight(), getModHeight(), DROP_DOWN_ARROW);
        RENDER_STACK.pop();

        // draw name
        double defaultHeight = getModHeight() / 3d * 2;
        double height = FONT_RENDERER.getStringWidth(module.getName(), defaultHeight) > getWidth() - getModHeight() - 2 ? FONT_RENDERER.getFontHeightWithCustomWidth(module.getName(), getWidth() - getModHeight() - 2) : defaultHeight;
        FONT_RENDERER.drawStringWithCustomHeight(module.getName(), getRenderX() + getModHeight(), getRenderY() + getModHeight() / 3d / 2d, module.getEnabled() ? color.getValue() : Color.WHITE, height);

        // draw line underneath
        RENDERER.drawLine(getRenderX(), getRenderY() + getHeight(), getRenderX() + getWidth(), getRenderY() + getHeight(), 1, color.getValue());
    }

    @Override
    public void click(int mouseX, int mouseY, int mouseButton) {
        if(isMouseOver(mouseX, mouseY) && getRenderY() + offset.getValue() + getModHeight() > mouseY) {
            if(mouseX >= getRenderX() + getModHeight()) {
                if(mouseButton == 0) module.toggle();
                else if(mouseButton == 1) toggleSettings();
            } else toggleSettings();
        }
        super.click(mouseX, mouseY, mouseButton);
    }

    @Override
    public boolean isMouseOver(double mouseX, double mouseY) {
        return mouseX >= getRenderX()
                && mouseX <= getRenderX() + getWidth()
                && mouseY >= getRenderY() + offset.getValue()
                && mouseY <= getRenderY() + offset.getValue() + getHeight()
                && getParent().isMouseOver(mouseX, mouseY);
    }

    public double getModHeight() {
        return modHeight.getValue();
    }

    // sets animation variables for toggling
    public void toggleSettings() {
        open.toggle();
    }

    public DynamicValue<Color> getColor() {
        return color;
    }

    public DynamicValue<Double> getOffset() {
        return offset;
    }

    public void setOffset(DynamicValue<Double> offset) {
        this.offset = offset;
    }
}
