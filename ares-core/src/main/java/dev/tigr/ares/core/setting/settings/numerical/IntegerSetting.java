package dev.tigr.ares.core.setting.settings.numerical;

import dev.tigr.ares.core.setting.SettingCategory;
import org.json.JSONObject;

/**
 * @author Tigermouthbear 7/5/20
 */
public class IntegerSetting extends NumberSetting<Integer> {
    public IntegerSetting(String name, int defaultValue, int min, int max) {
        super(name, defaultValue, min, max);
    }

    public IntegerSetting(SettingCategory parent, String name, int defaultValue, int min, int max) {
        this(name, defaultValue, min, max);
        setParent(parent);
    }

    @Override
    public Integer read(JSONObject jsonObject) {
        return jsonObject.getInt(getName());
    }
}
