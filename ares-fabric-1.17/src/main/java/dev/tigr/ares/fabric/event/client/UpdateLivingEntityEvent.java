package dev.tigr.ares.fabric.event.client;

import net.minecraft.entity.Entity;

public class UpdateLivingEntityEvent {
    private final Entity entity;

    public UpdateLivingEntityEvent(Entity entity) {
        this.entity = entity;
    }

    public Entity getEntity() {
        return entity;
    }

    public static class Pre extends UpdateLivingEntityEvent {
        public Pre(Entity entity) {
            super(entity);
        }
    }

    public static class Post extends UpdateLivingEntityEvent {
        public Post(Entity entity) {
            super(entity);
        }
    }
}
