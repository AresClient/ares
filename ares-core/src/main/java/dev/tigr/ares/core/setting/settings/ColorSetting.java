package dev.tigr.ares.core.setting.settings;

import dev.tigr.ares.core.setting.Setting;
import dev.tigr.ares.core.setting.SettingCategory;
import dev.tigr.ares.core.util.render.Color;
import org.json.JSONObject;

/**
 * @author Tigermouthbear 7/5/20
 */
public class ColorSetting extends Setting<Color> {
    public ColorSetting(String name, Color defaultValue) {
        super(name, defaultValue);
    }

    public ColorSetting(SettingCategory parent, String name, Color defaultValue) {
        super(parent, name, defaultValue);
    }

    @Override
    public Color read(JSONObject jsonObject) {
        return new Color(jsonObject.getInt(getName()));
    }

    @Override
    public void save(JSONObject jsonObject) {
        jsonObject.put(getName(), getValue().getRGB());
    }
}
