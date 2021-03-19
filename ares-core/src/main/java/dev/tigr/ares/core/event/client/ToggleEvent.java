package dev.tigr.ares.core.event.client;

import dev.tigr.ares.core.feature.module.Module;
import dev.tigr.simpleevents.event.Event;

public class ToggleEvent extends Event {
    private final Module module;
    private final boolean enabled;

    public ToggleEvent(Module module, boolean enabled) {
        this.module = module;
        this.enabled = enabled;
    }

    public Module getModule() {
        return module;
    }

    public boolean isEnabled() {
        return enabled;
    }
}
