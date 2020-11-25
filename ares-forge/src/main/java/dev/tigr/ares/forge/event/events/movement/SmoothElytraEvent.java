package dev.tigr.ares.forge.event.events.movement;

import dev.tigr.simpleevents.event.Event;

public class SmoothElytraEvent extends Event {
    public boolean isWorldRemote;

    public SmoothElytraEvent(boolean isWorldRemote) {
        this.isWorldRemote = isWorldRemote;
    }
}
