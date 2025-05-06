package org.aresclient.ares.api.setting;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.aresclient.ares.api.setting.settings.*;
import org.aresclient.ares.api.setting.settings.grouped.Group;
import org.aresclient.ares.api.setting.settings.grouped.GroupedSetting;
import org.aresclient.ares.api.setting.settings.grouped.IGroupMember;
import org.aresclient.ares.api.setting.settings.list.EnumListSetting;
import org.aresclient.ares.api.setting.settings.list.MapListSetting;
import org.aresclient.ares.api.setting.settings.list.StringListSetting;
import org.aresclient.ares.api.setting.settings.number.DoubleSetting;
import org.aresclient.ares.api.setting.settings.number.FloatSetting;
import org.aresclient.ares.api.setting.settings.number.IntegerSetting;
import org.aresclient.ares.api.setting.settings.number.LongSetting;

import java.util.*;
import java.util.function.Supplier;

public class MapSetting extends Setting<Map<String, Setting<?>>> {
    private JsonObject jsonObject = null;

    public MapSetting() {
        super(Type.MAP, new LinkedHashMap<>());
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

    @Override
    public void setDefault() {
        getValue().values().forEach(Setting::setDefault);
    }

    public Setting<?> find(String path) {
        if(path == null || path.isEmpty()) return this;

        Setting<?> curr = this;
        String[] split = path.split(":");
        for(String name: split) {
            if(curr instanceof MapSetting) {
                curr = ((MapSetting) curr).getValue().get(name);
            } else if(curr instanceof MapListSetting) {
                try {
                    curr = ((MapListSetting) curr).getValue().get(Integer.parseInt(name));
                } catch(NumberFormatException e) {
                    return null;
                }
            } else return null;
        }

        return curr;
    }

    protected <S extends Setting<?>> S add(S setting, String name, String... description) {
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

    public MapSetting addMap(String name, String... description) {
        return add(new MapSetting(), name, description);
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

    public MapListSetting addList(String name, String... description) {
        return add(new MapListSetting(), name, description);
    }

    public StringListSetting addStringList(String name, String... description) {
        return addStringList(name, new ArrayList<>(), description);
    }

    public StringListSetting addStringList(String name, ArrayList<String> defaultValue, String... description) {
        return add(new StringListSetting(defaultValue), name, description);
    }

    public <T extends Enum<?>> EnumListSetting<T> addEnumList(String name, Class<T> enumClass, List<T> defaultValue, String... description) {
        return add(new EnumListSetting<>(enumClass, defaultValue), name, description);
    }

    public <T extends Enum<?>> EnumListSetting<T> addEnumList(Class<T> enumClass, String name, String... description) {
        return addEnumList(name, enumClass, new ArrayList<>(), description);
    }

    public <T, V extends Group<T>> GroupedSetting<T, V> addGrouped(String name, ArrayList<V> defaultValue, Set<IGroupMember<T>> possibleMembers, Supplier<V> groupSupplier, String... description) {
        return add(new GroupedSetting<>(defaultValue, possibleMembers, groupSupplier), name, description);
    }
}
