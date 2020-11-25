package dev.tigr.ares.core.setting;

import dev.tigr.ares.core.util.function.DynamicValue;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a saveable setting
 *
 * @param <T> Type you are saving
 */
public abstract class Setting<T> extends Saveable<T> {
    private static final List<Setting> SETTINGS = new ArrayList<>();

    private SettingCategory parent = null;

    private DynamicValue<Boolean> visibility = () -> true;

    public Setting(String name, T value) {
        super(name, value);
        SETTINGS.add(this);
    }

    public Setting(SettingCategory parent, String name, T value) {
        this(name, value);
        setParent(parent);
    }

    public static List<Setting> getAll() {
        return SETTINGS;
    }

    public abstract T read(JSONObject jsonObject);

    public abstract void save(JSONObject jsonObject);

    public SettingCategory getParent() {
        return parent;
    }

    public void setParent(SettingCategory parent) {
        if(this.parent == null && parent != null) {
            this.parent = parent;
            parent.getSettings().add(this);
            if(parent.getValue().has(getName())) setValue(read(parent.getValue()));
        }
    }

    public boolean isVisible() {
        return visibility.getValue();
    }

    public Setting<T> setVisibility(DynamicValue<Boolean> visibility) {
        this.visibility = visibility;
        return this;
    }

    public void remove() {
        SETTINGS.remove(this);
    }
}
