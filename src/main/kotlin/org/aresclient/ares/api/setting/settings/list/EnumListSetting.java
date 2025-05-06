package org.aresclient.ares.api.setting.settings.list;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

import java.util.List;

public class EnumListSetting<T extends Enum<?>> extends AbstractListSetting<T> {
    private final Class<T> clazz;

    public EnumListSetting(Class<T> clazz, List<T> value) {
        super(Type.ENUM_LIST, value);
        this.clazz = clazz;
    }

    @Override
    public void read(JsonElement jsonElement) {
        values.clear();

        JsonArray jsonArray = jsonElement.getAsJsonArray();
        for(JsonElement childElement: jsonArray) {
            values.add(clazz.getEnumConstants()[childElement.getAsInt()]);
        }

        onChange();
    }

    @Override
    public JsonElement write() {
        JsonArray jsonArray = new JsonArray();
        for(T value: values) jsonArray.add(new JsonPrimitive(value.ordinal()));
        return jsonArray;
    }
}
