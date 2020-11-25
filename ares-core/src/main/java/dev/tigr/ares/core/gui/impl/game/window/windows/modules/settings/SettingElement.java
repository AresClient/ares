package dev.tigr.ares.core.gui.impl.game.window.windows.modules.settings;

import dev.tigr.ares.core.gui.api.Element;
import dev.tigr.ares.core.gui.api.GUI;
import dev.tigr.ares.core.gui.impl.game.window.windows.modules.ModuleElement;
import dev.tigr.ares.core.setting.Setting;
import dev.tigr.ares.core.setting.settings.BindSetting;
import dev.tigr.ares.core.setting.settings.BooleanSetting;
import dev.tigr.ares.core.setting.settings.EnumSetting;
import dev.tigr.ares.core.setting.settings.StringSetting;
import dev.tigr.ares.core.setting.settings.numerical.NumberSetting;

/**
 * @param <T> type of setting 7/3/20
 * @author Tigermouthbear
 */
public class SettingElement<T extends Setting<?>> extends Element {
    protected static final double PADDING = 1;

    protected final T setting;

    public SettingElement(GUI gui, T setting) {
        super(gui);

        this.setting = setting;
    }

    public static <T extends Setting<?>> SettingElement<?> create(GUI gui, Setting<?> setting) {
        if(setting instanceof BooleanSetting) return new BooleanSettingElement(gui, (BooleanSetting) setting);
        else if(setting instanceof BindSetting) return new BindSettingElement(gui, (BindSetting) setting);
        else if(setting instanceof EnumSetting) return new EnumSettingElement<>(gui, (EnumSetting<?>) setting);
        else if(setting instanceof StringSetting) return new StringSettingElement(gui, (StringSetting) setting);
        else if(setting instanceof NumberSetting) return new SliderSettingElement<>(gui, (NumberSetting<?>) setting);
        else return new SettingElement<>(gui, setting);
    }

    @Override
    public boolean isMouseOver(double mouseX, double mouseY) {
        return mouseX >= getRenderX()
                && mouseX <= getRenderX() + getWidth()
                && mouseY >= getRenderY() + ((ModuleElement) getParent()).getOffset().getValue()
                && mouseY <= getRenderY() + ((ModuleElement) getParent()).getOffset().getValue() + getHeight()
                && getParent().isMouseOver(mouseX, mouseY);
    }
}
