package org.aresclient.ares.api.setting.settings.number;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

public class DoubleSetting extends NumberSetting<Double> {
    public DoubleSetting(Double value) {
        super(Type.DOUBLE, value);
    }

    @Override
    public void setValue(Double value) {
        if (getPrecision() != null) {
            int scale = (int) Math.pow(10, getPrecision());
            value = (double) Math.round(value * scale) / scale;
        }

        super.setValue(value);
    }

    @Override
    public void read(JsonElement jsonElement) {
        setValue(jsonElement.getAsDouble());
    }

    @Override
    public JsonElement write() {
        return new JsonPrimitive(getValue());
    }
}
