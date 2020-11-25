package dev.tigr.ares.core.util.function;

/**
 * Functional interface for getting a dynamic value
 *
 * @author Tigermouthbear 6/16/20
 */
@FunctionalInterface
public interface DynamicValue<T> {
    /**
     * Lambda method for getting value
     *
     * @return T
     */
    T getValue();
}
