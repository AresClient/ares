package org.aresclient.ares.api.setting.settings.number;

import org.aresclient.ares.api.setting.Setting;

public abstract class NumberSetting<T extends Number> extends Setting<T> {
    private T min = null;
    private T max = null;

    /**
     * Does not affect long
     */
    private java.lang.Integer precision = null;

    NumberSetting(Type type, T value) {
        super(type, value);
    }

    public T getMin() {
        return min;
    }

    public org.aresclient.ares.api.setting.settings.number.NumberSetting<T> setMin(T min) {
        this.min = min;
        return this;
    }

    public T getMax() {
        return max;
    }

    public org.aresclient.ares.api.setting.settings.number.NumberSetting<T> setMax(T max) {
        this.max = max;
        return this;
    }

    public java.lang.Integer getPrecision() {
        return precision;
    }

    public org.aresclient.ares.api.setting.settings.number.NumberSetting<T> setPrecision(java.lang.Integer precision) {
        if (this.getValue() instanceof java.lang.Double
                || this.getValue() instanceof java.lang.Float
                || this.getValue() instanceof java.lang.Integer)
            this.precision = precision;
        return this;
    }
}
