package dev.tigr.ares.fabric.event.movement;

import dev.tigr.simpleevents.event.Event;
import net.minecraft.entity.Entity;

public class EntityPushEvent extends Event {
    private final Entity entity;

    public EntityPushEvent(Entity entity) {
        this.entity = entity;
    }

    public Entity getEntity() {
        return entity;
    }
}
