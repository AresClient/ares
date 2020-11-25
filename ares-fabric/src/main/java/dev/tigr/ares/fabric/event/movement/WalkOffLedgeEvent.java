package dev.tigr.ares.fabric.event.movement;

import dev.tigr.simpleevents.event.Event;

public class WalkOffLedgeEvent extends Event {
    public boolean isSneaking;

    public WalkOffLedgeEvent(boolean isSneaking) {
        this.isSneaking = isSneaking;
    }
}