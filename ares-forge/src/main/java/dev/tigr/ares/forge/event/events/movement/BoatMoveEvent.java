package dev.tigr.ares.forge.event.events.movement;

import dev.tigr.simpleevents.event.Event;
import net.minecraft.entity.item.EntityBoat;

public class BoatMoveEvent extends Event {
    private final EntityBoat boat;
    public double x;
    public double y;
    public double z;

    public BoatMoveEvent(EntityBoat boat, double x, double y, double z) {
        this.boat = boat;
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public EntityBoat getBoat() {
        return boat;
    }
}
