package dev.tigr.ares.core.setting.settings.numerical;

import dev.tigr.ares.core.setting.SettingCategory;
import org.json.JSONObject;

/**
 * @author Tigermouthbear 7/5/20
 */
public class DoubleSetting extends NumberSetting<Double> {
    public DoubleSetting(String name, double defaultValue, double min, double max) {
        super(name, defaultValue, min, max);
    }

    public DoubleSetting(SettingCategory parent, String name, double defaultValue, double min, double max) {
        super(parent, name, defaultValue, min, max);
    }

    @Override
    public Double read(JSONObject jsonObject) {
        return jsonObject.getDouble(getName());
    }
}
