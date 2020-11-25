package dev.tigr.ares.core.event.client;

import dev.tigr.ares.core.feature.module.Module;
import dev.tigr.simpleevents.event.Event;

public class ToggleEvent extends Event {
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
