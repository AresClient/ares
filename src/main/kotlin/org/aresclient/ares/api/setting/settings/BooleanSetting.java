package org.aresclient.ares.api.setting.settings;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import org.aresclient.ares.api.setting.Setting;

public class BooleanSetting extends Setting<Boolean> {
    public BooleanSetting(Boolean value) {
        super(Type.BOOLEAN, value);
    }

    @Override
    public void read(JsonElement jsonElement) {
        setValue(jsonElement.getAsBoolean());
    }

    @Override
    public JsonElement write() {
        return new JsonPrimitive(getValue());
    }
}
