package dev.tigr.ares.core.setting;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import java.util.function.Consumer;

/**
 * @author Tigermouthbear 7/5/20
 */
public class SettingCategory extends Saveable<JSONObject> {
    private static final List<SettingCategory> BASE_SETTING_CATEGORIES = new ArrayList<>();
    private final List<SettingCategory> settingCategories = new ArrayList<>();
    private final List<Setting> settings = new ArrayList<>();
    private SettingCategory parent = null;

    SettingCategory(JSONObject parent, String name) {
        super(name, new JSONObject());

        // read setting category from parent
        if(parent.has(name)) setValue(parent.getJSONObject(name));
        else parent.put(name, getValue());
    }

    public SettingCategory(SettingCategory parent, String name) {
        this(parent.getValue(), name);
        this.parent = parent;

        parent.getSettingCategories().add(this);
    }

    public SettingCategory(String name) {
        this(SettingsManager.SAVE_OBJECT, name);
        BASE_SETTING_CATEGORIES.add(this);
    }

    public static void forEach(Consumer<SettingCategory> consumer) {
        Stack<SettingCategory> stack = new Stack<>();
        stack.addAll(BASE_SETTING_CATEGORIES);
        while(!stack.empty()) {
            SettingCategory settingCategory = stack.pop();
            consumer.accept(settingCategory);
            stack.addAll(settingCategory.getSettingCategories());
        }
    }

    public SettingCategory getParent() {
        return parent;
    }

    public List<Setting> getSettings() {
        return settings;
    }

    public List<SettingCategory> getSettingCategories() {
        return settingCategories;
    }
}
