package dev.tigr.ares.core.setting.settings;

import dev.tigr.ares.core.setting.Setting;
import dev.tigr.ares.core.setting.SettingCategory;
import org.json.JSONObject;

/**
 * @author Tigermouthbear 7/5/20
 */
public class BooleanSetting extends Setting<Boolean> {
    public BooleanSetting(String name, boolean defaultValue) {
        super(name, defaultValue);
    }

    public BooleanSetting(SettingCategory parent, String name, boolean defaultValue) {
        super(parent, name, defaultValue);
    }

    @Override
    public Boolean read(JSONObject jsonObject) {
        return jsonObject.getBoolean(getName());
    }

    @Override
    public void save(JSONObject jsonObject) {
        jsonObject.put(getName(), getValue());
    }
}
