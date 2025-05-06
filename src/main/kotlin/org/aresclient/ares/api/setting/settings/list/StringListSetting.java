package org.aresclient.ares.api.setting.settings.list;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

import java.util.*;

public class StringListSetting extends AbstractListSetting<String> {
    public StringListSetting(List<String> value) {
        super(Type.STRING_LIST, value);
    }

    @Override
    public void read(JsonElement jsonElement) {
        values.clear();

        JsonArray jsonArray = jsonElement.getAsJsonArray();
        for(JsonElement childElement: jsonArray) {
            values.add(childElement.getAsString());
        }

        onChange();
    }

    @Override
    public JsonElement write() {
        JsonArray jsonArray = new JsonArray();
        for(String string: values) jsonArray.add(new JsonPrimitive(string));
        return jsonArray;
    }
}
