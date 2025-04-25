package org.aresclient.ares.api.setting.settings;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import org.aresclient.ares.api.setting.Setting;

import java.util.ArrayList;
import java.util.function.BiConsumer;

public class BindSetting extends Setting<Integer> {
    private static final java.util.List<org.aresclient.ares.api.setting.settings.BindSetting> BINDS = new ArrayList<>();

    private BiConsumer<Boolean, Boolean> callback = null;

    public BindSetting(java.lang.Integer value) {
        super(Type.BIND, value);
        BINDS.add(this);
    }

    public BiConsumer<Boolean, Boolean> getCallback() {
        return callback;
    }

    public org.aresclient.ares.api.setting.settings.BindSetting setCallback(BiConsumer<Boolean, Boolean> callback) {
        this.callback = callback;
        return this;
    }

    public static java.util.List<org.aresclient.ares.api.setting.settings.BindSetting> getAll() {
        return BINDS;
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
