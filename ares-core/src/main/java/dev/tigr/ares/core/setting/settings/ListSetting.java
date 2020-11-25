package dev.tigr.ares.core.setting.settings;

import dev.tigr.ares.core.setting.Setting;
import dev.tigr.ares.core.setting.SettingCategory;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;

/**
 * @author Tigermouthbear 7/5/20
 */
public class ListSetting<T> extends Setting<List<T>> {
    public ListSetting(String name, List<T> defaultValue) {
        super(name, defaultValue);
    }

    public ListSetting(SettingCategory parent, String name, List<T> defaultValue) {
        super(parent, name, defaultValue);
    }

    @Override
    public List<T> read(JSONObject jsonObject) {
        return (List<T>) jsonObject.getJSONArray(getName()).toList();
    }

    @Override
    public void save(JSONObject jsonObject) {
        jsonObject.put(getName(), new JSONArray(getValue()));
    }
}