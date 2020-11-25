package dev.tigr.ares.forge.event.events.player;

import dev.tigr.simpleevents.event.Event;

public class AntiHitboxEvent extends Event {
    private boolean allowed = false;

    public boolean getAllowed() {
        return allowed;
    }

    public void setAllowed(boolean value) {
        allowed = value;
    }
}
