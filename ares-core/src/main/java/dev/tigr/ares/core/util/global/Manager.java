package dev.tigr.ares.core.util.global;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * @param <T> type to manage
 * @author Tigermouthbear
 */
public class Manager<T> {
    private final ArrayList<T> instances = new ArrayList<>();
    private final ArrayList<Class<? extends T>> erroredClasses = new ArrayList<>();
    private final Consumer<T> onBuild;

    public Manager() {
        this(null);
    }

    public Manager(Consumer<T> onBuild) {
        this.onBuild = onBuild;
    }

    public Manager<T> initialize(List<Class<? extends T>> instances) {
        instances.forEach(this::build);
        return this;
    }

    public void build(Class<? extends T> clazz) {
        try {
            T instance = clazz.newInstance();
            instances.add(instance);
            if(onBuild != null) onBuild.accept(instance);
        } catch(Exception e) {
            erroredClasses.add(clazz);
            e.printStackTrace();
        }
    }

    public ArrayList<Class<? extends T>> getErroredClasses() {
        return erroredClasses;
    }

    public ArrayList<T> getInstances() {
        return instances;
    }
}
