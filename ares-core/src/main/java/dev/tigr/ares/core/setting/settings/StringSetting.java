package dev.tigr.ares.core.setting.settings;

import dev.tigr.ares.core.setting.Setting;
import dev.tigr.ares.core.setting.SettingCategory;
import org.json.JSONObject;

/**
 * @author Tigermouthbear 7/5/20
 */
public class StringSetting extends Setting<String> {
    public StringSetting(String name, String defaultValue) {
        super(name, defaultValue);
    }

    public StringSetting(SettingCategory parent, String name, String defaultValue) {
        super(parent, name, defaultValue);
    }

    @Override
    public String read(JSONObject jsonObject) {
        return jsonObject.getString(getName());
    }

    @Override
    public void save(JSONObject jsonObject) {
        jsonObject.put(getName(), getValue());
    }
}
