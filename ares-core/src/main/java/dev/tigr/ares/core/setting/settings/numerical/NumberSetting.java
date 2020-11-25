package dev.tigr.ares.core.setting.settings.numerical;

import dev.tigr.ares.core.setting.Setting;
import dev.tigr.ares.core.setting.SettingCategory;
import org.json.JSONObject;

/**
 * @param <T> type of number
 * @author Tigermouthbear 7/5/20
 */
public abstract class NumberSetting<T extends Number> extends Setting<T> {
    private final T min;
    private final T max;

    public NumberSetting(String name, T defaultValue, T min, T max) {
        super(name, defaultValue);
        this.min = min;
        this.max = max;
    }

    public NumberSetting(SettingCategory parent, String name, T defaultValue, T min, T max) {
        super(parent, name, defaultValue);
        this.min = min;
        this.max = max;
    }

    public T getMin() {
        return min;
    }

    public T getMax() {
        return max;
    }

    @Override
    public void save(JSONObject jsonObject) {
        jsonObject.put(getName(), getValue());
    }
}
