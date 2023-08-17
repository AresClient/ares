package org.aresclient.ares.api.setting;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.function.Consumer;

public abstract class Setting<T, R> {
    public enum Type {
        STRING, BOOLEAN, ENUM,
        COLOR, INTEGER, DOUBLE,
        FLOAT, LONG, BIND,
        GROUPED, LIST, MAP
    }

    private final Type type;
    private final java.lang.String name;
    private final Setting<?, R> parent;
    private final T defaultValue;
    protected T value;

    private final ISerializer<R> serializer;
    private boolean dirty = true;
    private R data;

    private Setting(Type type, java.lang.String name, ISerializer<R> serializer, T defaultValue) {
        this.type = type;
        this.name = name;
        this.parent = null;
        this.defaultValue = defaultValue;
        this.value = defaultValue;
        this.serializer = serializer;
    }

    private Setting(Type type, java.lang.String name, Setting<?, R> parent, T defaultValue) {
        this.type = type;
        this.name = name;
        this.parent = parent;
        this.defaultValue = this.value = defaultValue;
        this.serializer = parent.serializer;
    }

    public void read(R data) {
        this.data = data;
        dirty = false;
        serializer.read(this, data);
    }

    public R write() {
        if(dirty) {
            data = serializer.write(this);
            dirty = false;
        }
        return data;
    }

    public Type getType() {
        return type;
    }

    public java.lang.String getName() {
        return name;
    }

    public Setting<?, R> getParent() {
        return parent;
    }

    public T getDefaultValue() {
        return defaultValue;
    }

    public void defaults() {
        setValue(defaultValue);
    }

    public T getValue() {
        return value;
    }

    public void setValue(T value) {
        this.value = value;
        dirty = true;
    }

    public ISerializer<R> getSerializer() {
        return serializer;
    }

    public java.lang.String getPath() {
        return parent == null ? name : (parent.name == null ? "" : parent.name) + ":" + name;
    }

    public static class String<R> extends Setting<java.lang.String, R> {
        String(java.lang.String name, Setting<?, R> parent, java.lang.String defaultValue) {
            super(Type.STRING, name, parent, defaultValue);
        }
    }

    public static class Boolean<R> extends Setting<java.lang.Boolean, R> {
        Boolean(java.lang.String name, Setting<?, R> parent, java.lang.Boolean defaultValue) {
            super(Type.BOOLEAN, name, parent, defaultValue);
        }
    }

    public static class Enum<T extends java.lang.Enum<?>, R> extends Setting<T, R> {
        Enum(java.lang.String name, Setting<?, R> parent, T defaultValue) {
            super(Type.ENUM, name, parent, defaultValue);
        }
    }

    public static class Color<R> extends Setting<org.aresclient.ares.api.util.Color, R> {
        private boolean rainbow;

        Color(java.lang.String name, Setting<?, R> parent, org.aresclient.ares.api.util.Color defaultValue, boolean rainbow) {
            super(Type.COLOR, name, parent, defaultValue);
            this.rainbow = rainbow;
        }

        public void setValue(org.aresclient.ares.api.util.Color value, boolean rainbow) {
            setValue(value);
            this.rainbow = rainbow;
        }

        public boolean isRainbow() {
            return rainbow;
        }

        public void setRainbow(boolean rainbow) {
            this.rainbow = rainbow;
        }

        @Override
        public org.aresclient.ares.api.util.Color getValue() {
            return rainbow ? rainbow(0L) : super.getValue();
        }

        public org.aresclient.ares.api.util.Color getValue(java.lang.Long offset) {
            return rainbow ? rainbow(offset) : super.getValue();
        }

        public org.aresclient.ares.api.util.Color[] getValues(int size) {
            long offset = 10240L / size;

            org.aresclient.ares.api.util.Color[] values = new org.aresclient.ares.api.util.Color[size];
            for(int i = 0; i < size; i++) values[i] = getValue(offset);
            return values;
        }

        private static org.aresclient.ares.api.util.Color rainbow(java.lang.Long offset) {
            float hue = ((float) ((System.currentTimeMillis() + offset) % 10240L)) / 10240.0f;
            return new org.aresclient.ares.api.util.Color(
                    org.aresclient.ares.api.util.Color.HSBtoRGB(hue, 1.0f, 1.0f));
        }
    }

    public static class Bind<R> extends Setting<java.lang.Integer, R> {
        private static final java.util.List<Bind<?>> BINDS = new ArrayList<>();

        private final Consumer<java.lang.Boolean> callback;

        Bind(java.lang.String name, Setting<?, R> parent, java.lang.Integer defaultValue, Consumer<java.lang.Boolean> callback) {
            super(Type.BIND, name, parent, defaultValue);
            this.callback = callback;
            BINDS.add(this);
        }

        public Consumer<java.lang.Boolean> getCallback() {
            return callback;
        }

        public static java.util.List<Bind<?>> getAll() {
            return BINDS;
        }
    }

    public static class Number<T extends java.lang.Number, R> extends Setting<T, R> {
        private final T min;
        private final T max;

        private Number(Type type, java.lang.String name, Setting<?, R> parent, T defaultValue, T min, T max) {
            super(type, name, parent, defaultValue);
            this.min = min;
            this.max = max;
        }

        public T getMin() {
            return min;
        }

        public T getMax() {
            return max;
        }
    }

    public static class Integer<R> extends Number<java.lang.Integer, R> {
        Integer(java.lang.String name, Setting<?, R> parent, java.lang.Integer defaultValue, java.lang.Integer min, java.lang.Integer max) {
            super(Type.INTEGER, name, parent, defaultValue, min, max);
        }
    }

    public static class Double<R> extends Number<java.lang.Double, R> {
        Double(java.lang.String name, Setting<?, R> parent, java.lang.Double defaultValue, java.lang.Double min, java.lang.Double max) {
            super(Type.DOUBLE, name, parent, defaultValue, min, max);
        }
    }

    public static class Float<R> extends Number<java.lang.Float, R> {
        Float(java.lang.String name, Setting<?, R> parent, java.lang.Float defaultValue, java.lang.Float min, java.lang.Float max) {
            super(Type.FLOAT, name, parent, defaultValue, min, max);
        }
    }

    public static class Long<R> extends Number<java.lang.Long, R> {
        Long(java.lang.String name, Setting<?, R> parent, java.lang.Long defaultValue, java.lang.Long min, java.lang.Long max) {
            super(Type.LONG, name, parent, defaultValue, min, max);
        }
    }

    /*public static class GroupTrait {
        private final Setting settings;

        public GroupTrait(Serializable settings) {
            this.settings = settings;
        }

        public Settings getSettings() {
            return settings;
        }
    }

    public static class Group<T, R extends GroupTrait> {
        private final R trait;
        private final ArrayList<T> values;

        public Group(R trait, ArrayList<T> values) {
            this.trait = trait;
            this.values = values;
        }

        public R getTrait() {
            return trait;
        }

        public java.util.List<T> getValues() {
            return values;
        }
    }

    public static class Grouped<T, R extends GroupTrait> extends Setting<HashMap<T, R>> {
        // TODO:
        Grouped(java.lang.String name, Serializable parent, HashMap<T, R> defaultValue) {
            super(Type.GROUPED, name, parent, defaultValue);
        }
    }*/

    public static class List<T, R> extends Setting<java.util.List<T>, R> {
        private final Type elementType;

        List(Type elementType, java.lang.String name, Setting<?, R> parent, java.util.List<T> defaultValue) {
            super(Type.LIST, name, parent, defaultValue);
            this.elementType = elementType;
        }

        public Type getElementType() {
            return elementType;
        }
    }

    public static class Map<R> extends Setting<java.util.Map<java.lang.String, R>, R> {
        private final java.util.List<Setting<?, R>> settings = new ArrayList<>();

        public Map(ISerializer<R> serializer, java.lang.String name, java.util.Map<java.lang.String, R> value) {
            super(Type.MAP, name, serializer, value);
        }

        public Map(ISerializer<R> serializer) {
            super(Type.MAP, null, serializer, new HashMap<>());
        }

        Map(java.lang.String name, Setting<?, R> parent) {
            super(Type.MAP, name, parent, new HashMap<>());
        }

        @Override
        public void defaults() {
            settings.forEach(Setting::defaults);
        }

        @Override
        public java.util.Map<java.lang.String, R> getValue() {
            for(Setting<?, R> setting: settings) {
                if(!setting.dirty) continue;
                value.put(setting.getName(), setting.write());
            }
            return super.getValue();
        }

        @Override
        public void setValue(java.util.Map<java.lang.String, R> value) {
            defaults();
            for(Setting<?, R> setting: settings) {
                R data = value.get(setting.getName());
                if(data != null) setting.read(data);
            }
            super.setValue(value);
        }

        public <T extends Setting<?, R>> T add(T setting) {
            settings.add(setting);

            R data = value.get(setting.getName());
            if(data != null) setting.read(data);
            else value.put(setting.getName(), setting.write());

            return setting;
        }

        public Setting.String<R> addString(java.lang.String name, java.lang.String defaultValue) {
            return add(new Setting.String<>(name, this, defaultValue));
        }

        public Setting.Boolean<R> addBoolean(java.lang.String name, boolean defaultValue) {
            return add(new Setting.Boolean<>(name, this, defaultValue));
        }

        public <T extends java.lang.Enum<?>> Setting.Enum<T, R> addEnum(java.lang.String name, T defaultValue) {
            return add(new Setting.Enum<>(name, this, defaultValue));
        }

        public Setting.Color<R> addColor(java.lang.String name, org.aresclient.ares.api.util.Color defaultValue, boolean rainbow) {
            return add(new Setting.Color<>(name, this, defaultValue, rainbow));
        }

        public Setting.Color<R> addColor(java.lang.String name, org.aresclient.ares.api.util.Color defaultValue) {
            return addColor(name, defaultValue, false);
        }

        public Setting.Bind<R> addBind(java.lang.String name, int defaultValue, Consumer<java.lang.Boolean> callback) {
            return add(new Setting.Bind<>(name, this, defaultValue, callback));
        }

        public Setting.Integer<R> addInteger(java.lang.String name, int defaultValue, java.lang.Integer min, java.lang.Integer max) {
            return add(new Setting.Integer<>(name, this, defaultValue, min, max));
        }

        public Setting.Integer<R> addInteger(java.lang.String name, int defaultValue) {
            return addInteger(name, defaultValue, null, null);
        }

        public Setting.Double<R> addDouble(java.lang.String name, double defaultValue, java.lang.Double min, java.lang.Double max) {
            return add(new Setting.Double<>(name, this, defaultValue, min, max));
        }

        public Setting.Double<R> addDouble(java.lang.String name, double defaultValue) {
            return addDouble(name, defaultValue, null, null);
        }

        public Setting.Float<R> addFloat(java.lang.String name, float defaultValue, java.lang.Float min, java.lang.Float max) {
            return add(new Setting.Float<>(name, this, defaultValue, min, max));
        }

        public Setting.Float<R> addFloat(java.lang.String name, float defaultValue) {
            return addFloat(name, defaultValue, null, null);
        }

        public Setting.Long<R> addLong(java.lang.String name, long defaultValue, java.lang.Long min, java.lang.Long max) {
            return add(new Setting.Long<>(name, this, defaultValue, min, max));
        }

        /*public <S, R extends Setting.GroupTrait> Setting.Grouped<S, R> addGrouped(java.lang.String name, HashMap<S, R> defaultValue) {
            return add(new Setting.Grouped<>(name, this, defaultValue));
        }*/

        public <T> Setting.List<T, R> addList(Type elementType, java.lang.String name, java.util.List<T> defaultValue) {
            return add(new Setting.List<>(elementType, name, this, defaultValue));
        }

        public <T> Setting.List<T, R> addList(Type elementType, java.lang.String name) {
            return addList(elementType, name, new ArrayList<>());
        }

        public Setting.Map<R> addMap(java.lang.String name) {
            return add(new Setting.Map<>(name, this));
        }
    }
}
