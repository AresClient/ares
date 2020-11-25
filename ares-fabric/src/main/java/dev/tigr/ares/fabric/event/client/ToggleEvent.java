package dev.tigr.ares.fabric.event.client;

import dev.tigr.ares.core.feature.module.Module;

public class ToggleEvent {
    private final Module module;

    public ToggleEvent(Module module) {
        this.module = module;
    }

    public Module getModule() {
        return module;
    }

    public static class Enabled extends ToggleEvent {
        public Enabled(Module module) {
            super(module);
        }
    }

    public static class Disabled extends ToggleEvent {
        public Disabled(Module module) {
            super(module);
        }
    }
}
