package org.aresclient.ares.api.setting;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.function.Consumer;

public class Setting<T> {
    public enum Type {
        STRING, BOOLEAN, ENUM,
        COLOR, INTEGER, DOUBLE,
        FLOAT, LONG, BIND,
        GROUPED, LIST, MAP
    }

    private Setting<?> parent = null;
    private java.lang.String name = null;
    private java.lang.String[] description = null;
    private ReadInfo<T> readInfo = null;
    private final Type type;
    private T value;

    private Setting(Type type, T value) {
        this.type = type;
        this.value = value;
    }

    public Setting<?> getParent() {
        return parent;
    }

    public void setParent(Setting<?> parent) {
        this.parent = parent;
    }

    public java.lang.String getName() {
        if(this instanceof Setting.List<?>)
            return java.lang.String.valueOf(((Setting.List<?>) this).getValue().indexOf(value));
        return name;
    }

    public void setName(java.lang.String name) {
        this.name = name;
    }

    public java.lang.String[] getDescription() {
        return description;
    }

    public void setDescription(java.lang.String[] description) {
        this.description = description;
    }

    public ReadInfo<T> getReadInfo() {
        return readInfo;
    }

    public void setReadInfo(ReadInfo<T> readInfo) {
        this.readInfo = readInfo;
    }

    public Type getType() {
        return type;
    }

    public T getValue() {
        return value;
    }

    public void setValue(T value) {
        this.value = value;
    }

    public java.lang.String getPath() {
        java.lang.String prefix = null;
        if(getParent() != null) prefix = getParent().getName();
        return prefix == null ? getName() : prefix + ":" + getName();
    }

    public static class String extends Setting<java.lang.String> {
        public String(java.lang.String value) {
            super(Type.STRING, value);
        }
    }

    public static class Boolean extends Setting<java.lang.Boolean> {
        public Boolean(java.lang.Boolean value) {
            super(Type.BOOLEAN, value);
        }
    }

    public static class Enum<T extends java.lang.Enum<?>> extends Setting<T> {
        public Enum(T value) {
            super(Type.ENUM, value);
        }
    }

    public static class Color extends Setting<org.aresclient.ares.api.util.Color> {
        public Color(org.aresclient.ares.api.util.Color value) {
            super(Type.COLOR, value);
        }
    }

    public static class Bind extends Setting<java.lang.Integer> {
        private static final java.util.List<Bind> BINDS = new ArrayList<>();

        private Consumer<java.lang.Boolean> callback = null;

        public Bind(java.lang.Integer value) {
            super(Type.BIND, value);
            BINDS.add(this);
        }

        public Consumer<java.lang.Boolean> getCallback() {
            return callback;
        }

        public Setting.Bind setCallback(Consumer<java.lang.Boolean> callback) {
            this.callback = callback;
            return this;
        }

        public static java.util.List<Bind> getAll() {
            return BINDS;
        }
    }

    public static class Number<T extends java.lang.Number> extends Setting<T> {
        private T min = null;
        private T max = null;

        private Number(Type type, T value) {
            super(type, value);
        }

        public T getMin() {
            return min;
        }

        public Setting.Number<T> setMin(T min) {
            this.min = min;
            return this;
        }

        public T getMax() {
            return max;
        }

        public Setting.Number<T> setMax(T max) {
            this.max = max;
            return this;
        }
    }

    public static class Integer extends Setting.Number<java.lang.Integer> {
        public Integer(java.lang.Integer value) {
            super(Type.INTEGER, value);
        }
    }

    public static class Double extends Setting.Number<java.lang.Double> {
        public Double(java.lang.Double value) {
            super(Type.DOUBLE, value);
        }
    }

    public static class Float extends Setting.Number<java.lang.Float> {
        public Float(java.lang.Float value) {
            super(Type.FLOAT, value);
        }
    }

    public static class Long extends Setting.Number<java.lang.Long> {
        public Long(java.lang.Long value) {
            super(Type.LONG, value);
        }
    }

    // TODO: GROUPED

    public static class List<T extends Setting<?>> extends Setting<java.util.List<T>> {
        public List(java.util.List<T> value) {
            super(Type.LIST, value);
            for(T setting: value) setting.setParent(this);
        }

        public void add(T setting) {
            setting.setParent(this);
            getValue().add(setting);
        }

        public void remove(T setting) {
            setting.setParent(null);
            getValue().remove(setting);
        }
    }

    public static class Map<R> extends Setting<java.util.Map<java.lang.String, Setting<?>>> {
        private final ISerializer<R> serializer;
        private final java.util.Map<java.lang.String, R> data;

        Map(ISerializer<R> serializer, java.util.Map<java.lang.String, R> data, java.util.Map<java.lang.String, Setting<?>> value) {
            super(Type.MAP, value);
            this.serializer = serializer;
            this.data = data;
        }

        public Map(ISerializer<R> serializer, java.util.Map<java.lang.String, R> data) {
            this(serializer, data, new HashMap<>());
        }

        public Map(ISerializer<R> serializer) {
            this(serializer, new HashMap<>());
        }

        private <T, S extends Setting<T>> S add(ReadInfo<T> readInfo, java.lang.String name, java.lang.String... description) {
            Setting<?> prev = getValue().get(name);
            if(prev != null) {
                assert prev.getReadInfo().type != readInfo.type;
                return (S) prev;
            }

            S setting = (S) serializer.read(readInfo, data.get(name));
            setting.setParent(this);
            setting.setName(name);
            setting.setReadInfo(readInfo);
            setting.setDescription(description);
            getValue().put(name, setting);
            return setting;
        }

        public Setting.String addString(java.lang.String name, java.lang.String defaultValue, java.lang.String... description) {
            return add(new ReadInfo<>(Type.STRING, defaultValue), name, description);
        }

        public Setting.Boolean addBoolean(java.lang.String name, boolean defaultValue, java.lang.String... description) {
            return add(new ReadInfo<>(Type.BOOLEAN, defaultValue), name, description);
        }

        public <T extends java.lang.Enum<?>> Setting.Enum<T> addEnum(java.lang.String name, T defaultValue, java.lang.String... description) {
            return add(new ReadInfo<>(Type.ENUM, defaultValue), name, description);
        }

        public Setting.Color addColor(java.lang.String name, org.aresclient.ares.api.util.Color defaultValue, java.lang.String... description) {
            return add(new ReadInfo<>(Type.COLOR, defaultValue), name, description);
        }

        public Setting.Bind addBind(java.lang.String name, int defaultValue, java.lang.String... description) {
            return add(new ReadInfo<>(Type.BIND, defaultValue), name, description);
        }

        public Setting.Integer addInteger(java.lang.String name, int defaultValue, java.lang.String... description) {
            return add(new ReadInfo<>(Type.INTEGER, defaultValue), name, description);
        }

        public Setting.Double addDouble(java.lang.String name, double defaultValue, java.lang.String... description) {
            return add(new ReadInfo<>(Type.DOUBLE, defaultValue), name, description);
        }

        public Setting.Float addFloat(java.lang.String name, float defaultValue, java.lang.String... description) {
            return add(new ReadInfo<>(Type.FLOAT, defaultValue), name, description);
        }

        public Setting.Long addLong(java.lang.String name, long defaultValue, java.lang.String... description) {
            return add(new ReadInfo<>(Type.LONG, defaultValue), name, description);
        }

        /*public <S, R extends Setting.GroupTrait> Setting.Grouped<S, R> addGrouped(java.lang.String name, HashMap<S, R> defaultValue) {
            return add(new Setting.Grouped<>(name, this, defaultValue));
        }*/

        public <T extends Setting<?>> Setting.List<T> addList(Type elementType, java.lang.String name, java.util.List<T> defaultValue, java.lang.String... description) {
            return add(new ReadInfo<>(Type.LIST, elementType, defaultValue), name, description);
        }

        public <T extends Setting<?>> Setting.List<T> addList(Type elementType, java.lang.String name, java.lang.String... description) {
            return addList(elementType, name, new ArrayList<>(), description);
        }

        public Setting.Map<R> addMap(java.lang.String name, java.lang.String... description) {
            return add(new ReadInfo<>(Type.MAP, new HashMap<>()), name, description);
        }

        public ISerializer<R> getSerializer() {
            return serializer;
        }
    }

    public static class ReadInfo<T> {
        private final Type type;
        private final Type elementType;
        private final T defaultValue;

        ReadInfo(Type type, Type elementType, T defaultValue) {
            this.type = type;
            this.elementType = elementType;
            this.defaultValue = defaultValue;
        }

        public ReadInfo(Type type, T defaultValue) {
            this(type, null, defaultValue);
        }

        public Type getType() {
            return type;
        }

        public Type getElementType() {
            return elementType;
        }

        public T getDefaultValue() {
            return defaultValue;
        }
    }
}
