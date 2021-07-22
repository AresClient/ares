package dev.tigr.ares.fabric.event.movement;

import dev.tigr.simpleevents.event.Event;
import net.minecraft.entity.vehicle.BoatEntity;

public class BoatMoveEvent extends Event {
    private final BoatEntity boat;
    public double x;
    public double y;
    public double z;

    public BoatMoveEvent(BoatEntity boat, double x, double y, double z) {
        this.boat = boat;
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public BoatEntity getBoat() {
        return boat;
    }
}
