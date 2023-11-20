package org.aresclient.ares.api.setting;

import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class Grouped<T extends Grouped.Trait, R extends Enum<?>> {
    private final Setting.List<Setting.Map<?>> groupsSetting;
    private final Class<T> traitClass;
    private final Class<R> enumClass;
    private final List<Group<T, R>> groups;
    private final Map<R, T> cache = new HashMap<>();

    @SafeVarargs
    public Grouped(Setting.Map<?> parent, String name, String[] description, Class<T> traitClass, Class<R> enumClass, Initializer<T, R> ...defaultGroups) {
        boolean isDefault = !parent.getData().containsKey(name);

        this.traitClass = traitClass;
        this.enumClass = enumClass;
        this.groupsSetting = parent.addList(Setting.Type.MAP, name, description);

        if(isDefault) {
            this.groups = Arrays.stream(defaultGroups).map(initializer -> initializer.accept(new Setting.Map<>(parent.getSerializer(), new HashMap<>()))).collect(Collectors.toList());
            write();
        } else {
            this.groups = new ArrayList<>();
            read();
        }
    }

    @SafeVarargs
    public Grouped(Setting.Map<?> parent, String name, Class<T> traitClass, Class<R> enumClass, Initializer<T, R> ...defaultGroups) {
        this(parent, name, new String[0], traitClass, enumClass, defaultGroups);
    }

    public T trait(R value) {
        return cache.computeIfAbsent(value, key -> {
            for(Group<T,R> group: groups) {
                for(R val: group.values) {
                    if(val == key) return group.trait;
                }
            }
            return null;
        });
    }

    public void transform(Consumer<List<Group<T, R>>> transformer) {
        transformer.accept(groups);
        write();
    }

    private void read() {
        groups.clear();
        cache.clear();

        int i = 0;
        for(Setting<?> setting: ((Setting.List<?>) groupsSetting).getValue()) {
            Setting.Map<?> map = (Setting.Map<?>) setting;
            try {
                groups.add(new Group<>(
                        map.addString("name", String.valueOf(i++)).getValue(),
                        (T) traitClass.getConstructor(Setting.Map.class).newInstance(map.addMap("trait")),
                        Arrays.stream(((Setting.List<?>) map.addEnumList(Setting.Type.ENUM, enumClass, "values")).getValue()).map(a -> (R) a.getValue()).collect(Collectors.toList())
                ));
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private void write() {
        groupsSetting.setDefault(); // clears list
        cache.clear();

        ISerializer<?> serializer = groupsSetting.getSerializer();
        for(Group<T, R> group: groups) {
            Setting.Map<?> map = new Setting.Map<>(serializer, new HashMap<>());
            map.addString("name", group.name);
            map.getValue().put("trait", group.trait.settings);

            Setting.Enum<R>[] values = new Setting.Enum[group.values.size()];
            for(int i = 0; i < group.values.size(); i++) values[i] = new Setting.Enum<>(serializer, group.values.get(i));
            map.getValue().put("values", new Setting.List<>(serializer, values));

            groupsSetting.add(map);
        }
    }

    public static interface Initializer<T extends Trait, R> {
        Group<T, R> accept(Setting.Map<?> map);
    }

    public static abstract class Trait {
        protected final Setting.Map<?> settings;

        protected Trait(Setting.Map<?> settings) {
            this.settings = settings;
        }
    }

    public static class Group<T extends Trait, R> {
        private final String name;
        private final T trait;
        private final java.util.List<R> values;

        public Group(String name, T trait, List<R> values) {
            this.name = name;
            this.trait = trait;
            this.values = values;
        }

        public String getName() {
            return name;
        }

        public T getTrait() {
            return trait;
        }

        public java.util.List<R> getValues() {
            return values;
        }
    }
}
