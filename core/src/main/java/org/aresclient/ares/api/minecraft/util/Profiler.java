package org.aresclient.ares.api.minecraft.util;

public interface Profiler {
    void push(String name);
    void pop();
}
