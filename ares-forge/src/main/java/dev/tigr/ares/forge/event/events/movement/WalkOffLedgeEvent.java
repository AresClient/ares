package dev.tigr.ares.forge.event.events.movement;

import dev.tigr.simpleevents.event.Event;

public class WalkOffLedgeEvent extends Event {
    public boolean isSneaking;

    public WalkOffLedgeEvent(boolean isSneaking) {
        this.isSneaking = isSneaking;
    }
}
