package dev.tigr.ares.core.util.function;

/**
 * Functional interface used for passing an action through a method
 *
 * @author Tigermouthbear 6/16/20
 */
@FunctionalInterface
public interface Hook {
    void invoke();
}
