package dev.tigr.ares.fabric.event.player;

import dev.tigr.simpleevents.event.Event;

public class InteractEvent extends Event {
    public boolean usingItem;

    public InteractEvent(boolean usingItem) {
        this.usingItem = usingItem;
    }
}
