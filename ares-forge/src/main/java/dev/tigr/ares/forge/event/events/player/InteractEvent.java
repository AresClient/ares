package dev.tigr.ares.forge.event.events.player;

import dev.tigr.simpleevents.event.Event;

public class InteractEvent extends Event {
    public boolean usingItem;

    public InteractEvent(boolean usingItem) {
        this.usingItem = usingItem;
    }
}
