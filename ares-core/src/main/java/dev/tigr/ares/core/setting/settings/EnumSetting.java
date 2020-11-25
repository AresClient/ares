package dev.tigr.ares.core.setting.settings;

import dev.tigr.ares.core.setting.Setting;
import dev.tigr.ares.core.setting.SettingCategory;
import org.json.JSONObject;

/**
 * @author Tigermouthbear 7/5/20
 */
public class EnumSetting<T extends Enum> extends Setting<T> {
    private final Enum<?>[] modes;

    public EnumSetting(String name, T defaultMode) {
        super(name, defaultMode);
        modes = defaultMode.getClass().getEnumConstants();
    }

    public EnumSetting(SettingCategory parent, String name, T defaultMode) {
        super(parent, name, defaultMode);
        modes = defaultMode.getClass().getEnumConstants();
    }

    public Enum<?>[] getModes() {
        return modes;
    }

    @Override
    public T read(JSONObject jsonObject) {
        String value = jsonObject.getString(getName());
        for(Enum enumConstant: modes) {
            if(enumConstant.name().equalsIgnoreCase(value))
                return (T) enumConstant;
        }

        return getValue();
    }

    @Override
    public void save(JSONObject jsonObject) {
        jsonObject.put(getName(), getValue().name());
    }
}
