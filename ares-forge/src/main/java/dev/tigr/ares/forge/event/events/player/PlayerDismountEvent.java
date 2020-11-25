package dev.tigr.ares.forge.event.events.player;

import dev.tigr.simpleevents.event.Event;
import net.minecraft.entity.Entity;

public class PlayerDismountEvent extends Event {
    public Entity entity;

    public PlayerDismountEvent(Entity entity) {
        this.entity = entity;
    }

    public static class Start extends PlayerDismountEvent {
        public Start(Entity entity) {
            super(entity);
        }
    }

    public static class End extends Event {
    }
}
