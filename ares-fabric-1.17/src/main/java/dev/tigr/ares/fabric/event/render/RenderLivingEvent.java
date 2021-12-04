package dev.tigr.ares.fabric.event.render;

import net.minecraft.entity.Entity;

public class RenderLivingEvent {
    private final Entity entity;

    public RenderLivingEvent(Entity entity) {
        this.entity = entity;
    }

    public Entity getEntity() {
        return entity;
    }

    public static class Pre extends RenderLivingEvent {
        public Pre(Entity entity) {
            super(entity);
        }
    }

    public static class Post extends RenderLivingEvent {
        public Post(Entity entity) {
            super(entity);
        }
    }
}
