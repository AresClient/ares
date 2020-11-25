package dev.tigr.ares.core.setting;

/**
 * Represents an object which is saveable to the json config
 *
 * @param <T> type of object being stored
 * @author Tigermouthbear 7/5/20
 */
public class Saveable<T> {
    private final String name;
    private T value;

    public Saveable(String name, T value) {
        this.name = name;
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public T getValue() {
        return value;
    }

    public void setValue(T value) {
        this.value = value;
    }
}
