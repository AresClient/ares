package org.aresclient.ares.api.setting.settings.number;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

public class IntegerSetting extends NumberSetting<Integer> {
    public IntegerSetting(Integer value) {
        super(Type.INTEGER, value);
    }

    @Override
    public void setValue(Integer value) {
        if(getPrecision() != null) {
            int scale = (int) Math.pow(10, getPrecision());
            value = (int) Math.round(value.doubleValue() / scale) * scale;
        }

        super.setValue(value);
    }

    @Override
    public void read(JsonElement jsonElement) {
        setValue(jsonElement.getAsInt());
    }

    @Override
    public JsonElement write() {
        return new JsonPrimitive(getValue());
    }
}
