package org.aresclient.ares.api.setting.settings;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import kotlin.Pair;
import org.aresclient.ares.api.setting.Setting;

import java.util.HashMap;
import java.util.function.Supplier;

public class EnumSetting<T extends Enum<?>> extends Setting<T> {
    private final HashMap<T, Pair<Supplier<Boolean>, String>> restrictions = new HashMap<>();

    public EnumSetting(T value) {
        super(Type.ENUM, value);
    }

    @Override
    public void setValue(T value) {
        for (java.util.Map.Entry<T, Pair<Supplier<Boolean>, String>> restriction : restrictions.entrySet()) {
            if (value == restriction.getKey() && restriction.getValue().getFirst().get()) {
                // TODO: Error Message - "Error setting Enum value: " + restriction.getValue().getSecond()
                return;
            }
        }
        super.setValue(value);
    }

    public EnumSetting<T> addRestriction(T value, Supplier<Boolean> restrictor, String errorMessage) {
        restrictions.put(value, new Pair<>(restrictor, errorMessage));
        return this;
    }

    @Override
    public void read(JsonElement jsonElement) {
        setValue((T) getValue().getClass().getEnumConstants()[jsonElement.getAsInt()]);
    }

    @Override
    public JsonElement write() {
        return new JsonPrimitive(getValue().ordinal());
    }
}
