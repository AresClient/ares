package org.aresclient.ares.api.setting.settings;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.aresclient.ares.api.setting.Setting;
import org.aresclient.ares.api.util.Color;

public class ColorSetting extends Setting<Color> {
    private final boolean defaultRainbow;
    private boolean rainbow;

    public ColorSetting(org.aresclient.ares.api.util.Color value, boolean rainbow) {
        super(Type.COLOR, value);
        this.defaultRainbow = rainbow;
        this.rainbow = rainbow;
    }

    public ColorSetting(org.aresclient.ares.api.util.Color value) {
        this(value, false);
    }

    @Override
    public org.aresclient.ares.api.util.Color getValue() {
        return rainbow ? org.aresclient.ares.api.util.Color.rainbow().deriveAlpha(super.getValue().getAlpha()) : super.getValue();
    }

    @Override
    public void setDefault() {
        this.rainbow = defaultRainbow;
        super.setDefault();
    }

    public void setRed(float red) {
        super.setValue(super.getValue().deriveRed(red));
    }

    public void setGreen(float green) {
        super.setValue(super.getValue().deriveGreen(green));
    }

    public void setBlue(float blue) {
        super.setValue(super.getValue().deriveBlue(blue));
    }

    public void setAlpha(float alpha) {
        super.setValue(super.getValue().deriveAlpha(alpha));
    }

    public boolean isRainbow() {
        return rainbow;
    }

    public void setRainbow(boolean rainbow) {
        this.rainbow = rainbow;
    }

    @Override
    public void read(JsonElement jsonElement) {
        JsonObject colorObject = jsonElement.getAsJsonObject();
        setValue(new Color(
                colorObject.get("red").getAsFloat(),
                colorObject.get("green").getAsFloat(),
                colorObject.get("blue").getAsFloat(),
                colorObject.get("alpha").getAsFloat()
        ));
        setRainbow(colorObject.get("rainbow").getAsBoolean());
    }

    @Override
    public JsonElement write() {
        JsonObject colorObject = new JsonObject();
        colorObject.addProperty("red", getValue().getRed());
        colorObject.addProperty("green", getValue().getGreen());
        colorObject.addProperty("blue", getValue().getBlue());
        colorObject.addProperty("alpha", getValue().getAlpha());
        colorObject.addProperty("rainbow", isRainbow());
        return colorObject;
    }
}
