package org.aresclient.ares.api.setting.settings.number;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

public class FloatSetting extends NumberSetting<Float> {
    public FloatSetting(Float value) {
        super(Type.FLOAT, value);
    }

    @Override
    public void setValue(Float value) {
        if (getPrecision() != null) {
            int scale = (int) Math.pow(10, getPrecision());
            value = (float) Math.round(value * scale) / scale;
        }

        super.setValue(value);
    }

    @Override
    public void read(JsonElement jsonElement) {
        setValue(jsonElement.getAsFloat());
    }

    @Override
    public JsonElement write() {
        return new JsonPrimitive(getValue());
    }
}
