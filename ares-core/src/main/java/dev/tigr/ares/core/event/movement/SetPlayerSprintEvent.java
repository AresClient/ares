package dev.tigr.ares.core.event.movement;

import dev.tigr.simpleevents.event.Event;

public class SetPlayerSprintEvent extends Event {
    private boolean sprinting;

    public SetPlayerSprintEvent(boolean sprinting) {
        this.sprinting = sprinting;
    }

    public boolean isSprinting() {
        return sprinting;
    }

    public SetPlayerSprintEvent setSprinting(boolean sprinting) {
        this.sprinting = sprinting;
        return this;
    }
}
