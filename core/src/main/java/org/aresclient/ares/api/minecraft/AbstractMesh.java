package org.aresclient.ares.api.minecraft;

/**
 * This class represents a Mesh wrapped class, it provides the value management
 * for the minecraft value.
 *
 * @author Tigermouthbear 1/10/22
 * @param <T> The type of the Mesh class being wrapped
 */
public abstract class AbstractMesh<T> {
    private final T value;

    public AbstractMesh(T value) {
        this.value = value;
    }

    public T getMeshValue() {
        return value;
    }

    public boolean isNull() {
        return value == null;
    }

    public boolean isNotNull() {
        return value != null;
    }

    public boolean equals(Object obj) {
        if(obj instanceof AbstractMesh) {
            AbstractMesh<?> abstractMesh = (AbstractMesh<?>) obj;
            return value.equals(abstractMesh.getMeshValue());
        }
        return value.equals(obj);
    }
}
