package org.aresclient.ares.impl.minecraft.util;

import net.minecraft.util.profiler.Profiler;
import org.aresclient.ares.api.minecraft.AbstractMesh;

public class ProfilerMesh extends AbstractMesh<Profiler> implements org.aresclient.ares.api.minecraft.util.Profiler {
    public ProfilerMesh(Profiler value) {
        super(value);
    }

    @Override
    public void push(String name) {
        getMeshValue().push(name);
    }

    @Override
    public void pop() {
        getMeshValue().pop();
    }
}
