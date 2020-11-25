package dev.tigr.ares.core.setting.settings;

import dev.tigr.ares.core.setting.Setting;
import dev.tigr.ares.core.setting.SettingCategory;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * @author Tigermouthbear 7/5/20
 */
public class BindSetting extends Setting<String> {
    private static final List<BindSetting> BIND_SETTINGS = new ArrayList<>();

    private final Consumer<Setting<String>> press;

    public BindSetting(String name, String defaultKey, Consumer<Setting<String>> press) {
        super(name, defaultKey);
        this.press = press;
        BIND_SETTINGS.add(this);
    }

    public BindSetting(SettingCategory parent, String name, String defaultKey, Consumer<Setting<String>> press) {
        super(name, defaultKey);
        this.press = press;
        BIND_SETTINGS.add(this);
    }

    public static List<BindSetting> getBinds() {
        return BIND_SETTINGS;
    }

    public void invoke() {
        press.accept(this);
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
