package dev.tigr.ares.core.event.movement;

import dev.tigr.simpleevents.event.Event;

public class EntityClipEvent extends Event {
    private final int entity;

    public EntityClipEvent(int entity) {
        this.entity = entity;
    }

    public int getEntity() {
        return entity;
    }
}
