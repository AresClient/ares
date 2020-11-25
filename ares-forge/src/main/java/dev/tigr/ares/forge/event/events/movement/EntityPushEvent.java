package dev.tigr.ares.forge.event.events.movement;

import dev.tigr.simpleevents.event.Event;
import net.minecraft.entity.Entity;

public class EntityPushEvent extends Event {
    private final Entity entity;
    private double x;
    private double y;
    private double z;

    public EntityPushEvent(Entity entity, double x, double y, double z) {
        this.entity = entity;
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public double getY() {
        return y;
    }

    public void setY(double y) {
        this.y = y;
    }

    public double getZ() {
        return z;
    }

    public void setZ(double z) {
        this.z = z;
    }

    public Entity getEntity() {
        return entity;
    }
}
