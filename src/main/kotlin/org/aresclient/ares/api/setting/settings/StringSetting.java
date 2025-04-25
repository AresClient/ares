package org.aresclient.ares.api.setting.settings;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import org.aresclient.ares.api.setting.Setting;

public class StringSetting extends Setting<java.lang.String> {
    public StringSetting(java.lang.String value) {
        super(Type.STRING, value);
    }

    @Override
    public void read(JsonElement jsonElement) {
        setValue(jsonElement.getAsString());
    }

    @Override
    public JsonElement write() {
        return new JsonPrimitive(getValue());
    }
}
