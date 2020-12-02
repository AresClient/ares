package dev.tigr.ares.fabric.event.client;

import net.minecraft.entity.Entity;

public class EntityEvent {
    private final Entity entity;

    public EntityEvent(Entity entity) {
        this.entity = entity;
    }

    public Entity getEntity() {
        return entity;
    }

    public static class Spawn extends EntityEvent {
        public Spawn(Entity entity) {
            super(entity);
        }
    }

    public static class Remove extends EntityEvent {
        public Remove(Entity entity) {
            super(entity);
        }
    }
}
