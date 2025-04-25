package org.aresclient.ares.api.setting.settings.number;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

public class LongSetting extends NumberSetting<Long> {
    public LongSetting(Long value) {
        super(Type.LONG, value);
    }

    @Override
    public void read(JsonElement jsonElement) {
        setValue(jsonElement.getAsLong());
    }

    @Override
    public JsonElement write() {
        return new JsonPrimitive(getValue());
    }
}
