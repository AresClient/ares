package dev.tigr.ares.core.setting.settings.numerical;

import dev.tigr.ares.core.setting.SettingCategory;
import org.json.JSONObject;

/**
 * @author Tigermouthbear 7/5/20
 */
public class FloatSetting extends NumberSetting<Float> {
    public FloatSetting(String name, float defaultValue, float min, float max) {
        super(name, defaultValue, min, max);
    }

    public FloatSetting(SettingCategory parent, String name, float defaultValue, float min, float max) {
        super(parent, name, defaultValue, min, max);
    }

    @Override
    public Float read(JSONObject jsonObject) {
        return jsonObject.getFloat(getName());
    }
}
