package org.aresclient.ares.api.setting.settings.list;

import org.aresclient.ares.api.setting.Setting;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.Consumer;

public abstract class AbstractListSetting<T> extends Setting<List<T>> implements List<T> {
    private final List<T> defaultValues;
    protected final List<T> values;

    public AbstractListSetting(Type type, List<T> values) {
        super(type, null);
        this.defaultValues = new ArrayList<>(values);
        this.values = new ArrayList<>(values);
        for(T value: values) onAdd(value);
    }

    @Override
    public List<T> getValue() {
        return this;
    }

    @Override
    public void setValue(List<T> value) {
        throw new RuntimeException("Method not allowed");
    }

    @Override
    public void setDefault() {
        for(T value: values) onRemove(value);
        values.clear();
        addAll(defaultValues);
    }

    protected void onAdd(T value) {
    }

    protected void onRemove(T value) {
    }

    @Override
    protected void onChange() {
        for(Consumer<List<T>> listener: getListeners()) listener.accept(this);
    }

    @Override
    public int size() {
        return values.size();
    }

    @Override
    public boolean isEmpty() {
        return values.isEmpty();
    }

    @Override
    public boolean contains(Object o) {
        return values.contains(o);
    }

    @Override
    public @NotNull Iterator<T> iterator() {
        return values.iterator();
    }

    @Override
    public @NotNull Object @NotNull [] toArray() {
        return values.toArray();
    }

    @Override
    public @NotNull <V> V @NotNull [] toArray(@NotNull V @NotNull [] arr) {
        return values.toArray(arr);
    }

    @Override
    public boolean add(T value) {
        boolean result = values.add(value);
        onAdd(value);
        onChange();
        return result;
    }

    @Override
    public boolean remove(Object o) {
        boolean result = values.remove(o);
        onRemove((T) o);
        onChange();
        return result;
    }

    @Override
    public boolean containsAll(@NotNull Collection<?> collection) {
        return new HashSet<>(values).containsAll(collection);
    }

    @Override
    public boolean addAll(@NotNull Collection<? extends T> collection) {
        boolean result = values.addAll(collection);
        for(T value: collection) onAdd(value);
        onChange();
        return result;
    }

    @Override
    public boolean addAll(int i, @NotNull Collection<? extends T> collection) {
        boolean result = values.addAll(i, collection);
        for(T value: collection) onAdd(value);
        onChange();
        return result;
    }

    @Override
    public boolean removeAll(@NotNull Collection<?> collection) {
        boolean result = values.removeAll(collection);
        for(Object value: collection) onRemove((T) value);
        onChange();
        return result;
    }

    @Override
    public boolean retainAll(@NotNull Collection<?> collection) {
        boolean result = values.retainAll(collection);
        for(Object value: collection) onRemove((T) value);
        onChange();
        return result;
    }

    @Override
    public void clear() {
        for(T value: values) onRemove(value);
        values.clear();
        onChange();
    }

    @Override
    public T get(int i) {
        return values.get(i);
    }

    @Override
    public T set(int i, T value) {
        T result = values.set(i, value);
        onRemove(result);
        onAdd(value);
        onChange();
        return result;
    }

    @Override
    public void add(int i, T value) {
        values.add(i, value);
        onAdd(value);
        onChange();
    }

    @Override
    public T remove(int i) {
        T result = values.remove(i);
        onRemove(result);
        onChange();
        return result;
    }

    @Override
    public int indexOf(Object o) {
        return values.indexOf(o);
    }

    @Override
    public int lastIndexOf(Object o) {
        return values.lastIndexOf(o);
    }

    @Override
    public @NotNull ListIterator<T> listIterator() {
        return values.listIterator();
    }

    @Override
    public @NotNull ListIterator<T> listIterator(int i) {
        return values.listIterator(i);
    }

    @Override
    public @NotNull List<T> subList(int i, int i1) {
        return values.subList(i, i1);
    }
}
