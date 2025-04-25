package org.aresclient.ares.api.setting;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.aresclient.ares.api.setting.settings.*;
import org.aresclient.ares.api.setting.settings.number.DoubleSetting;
import org.aresclient.ares.api.setting.settings.number.FloatSetting;
import org.aresclient.ares.api.setting.settings.number.IntegerSetting;
import org.aresclient.ares.api.setting.settings.number.LongSetting;

import java.util.LinkedHashMap;
import java.util.Map;

public class SettingGroup extends Setting<Map<String, Setting<?>>> {
    private JsonObject jsonObject;

    public SettingGroup() {
        this(null);
    }

    public SettingGroup(JsonObject jsonObject) {
        super(Type.MAP, new LinkedHashMap<>());
        this.jsonObject = jsonObject;
    }

    @Override
    public void read(JsonElement jsonElement) {
        jsonObject = jsonElement.getAsJsonObject();
        for(Map.Entry<String, Setting<?>> entry: getValue().entrySet())
            entry.getValue().read(jsonObject.get(entry.getKey()));
    }

    @Override
    public JsonElement write() {
        JsonObject jsonObject = new JsonObject();
        for(Map.Entry<String, Setting<?>> entry: getValue().entrySet())
            jsonObject.add(entry.getKey(), entry.getValue().write());
        return jsonObject;
    }

    public <S extends Setting<?>> S add(S setting, String name, String... description) {
        if(getValue().containsKey(name)) return (S) getValue().get(name);
        getValue().put(name, setting);

        setting.setName(name);
        setting.setDescription(description);
        setting.setParent(this);

        if(jsonObject != null) {
            JsonElement jsonElement = jsonObject.get(name);
            if(jsonElement != null) setting.read(jsonElement);
        }

        return setting;
    }

    public SettingGroup addGroup(String name, String... description) {
        return add(new SettingGroup(), name, description);
    }

    public StringSetting addString(String name, String defaultValue, String... description) {
        return add(new StringSetting(defaultValue), name, description);
    }

    public BooleanSetting addBoolean(String name, boolean defaultValue, String... description) {
        return add(new BooleanSetting(defaultValue), name, description);
    }

    public <T extends Enum<?>> EnumSetting<T> addEnum(String name, T defaultValue, String... description) {
        return add(new EnumSetting<>(defaultValue), name, description);
    }

    public ColorSetting addColor(String name, org.aresclient.ares.api.util.Color defaultValue, boolean rainbow, String... description) {
        return add(new ColorSetting(defaultValue, rainbow), name, description);
    }

    public ColorSetting addColor(String name, org.aresclient.ares.api.util.Color defaultValue, String... description) {
        return addColor(name, defaultValue, false, description);
    }

    public BindSetting addBind(String name, int defaultValue, String... description) {
        return add(new BindSetting(defaultValue), name, description);
    }

    public IntegerSetting addInteger(String name, int defaultValue, String... description) {
        return add(new IntegerSetting(defaultValue), name, description);
    }

    public DoubleSetting addDouble(String name, double defaultValue, String... description) {
        return add(new DoubleSetting(defaultValue), name, description);
    }

    public FloatSetting addFloat(String name, float defaultValue, String... description) {
        return add(new FloatSetting(defaultValue), name, description);
    }

    public LongSetting addLong(String name, long defaultValue, String... description) {
        return add(new LongSetting(defaultValue), name, description);
    }

    public ListSetting addList(String name, String... description) {
        return add(new ListSetting(), name, description);
    }

    /*public <T extends EnumSetting<?>> ListSetting<T> addEnumList(Setting.Type elementType, Class<? extends Enum> enumClass, String name, T[] defaultValue, String... description) {
        return add(new ReadInfo<>(Setting.Type.LIST, elementType, defaultValue).setEnumClass(enumClass), name, description);
    }

    public <T extends EnumSetting<?>> ListSetting<T> addEnumList(Setting.Type elementType, Class<? extends Enum> enumClass, String name, String... description) {
        return addEnumList(elementType, enumClass, name, (T[]) new EnumSetting[]{}, description);
    }*/
}
