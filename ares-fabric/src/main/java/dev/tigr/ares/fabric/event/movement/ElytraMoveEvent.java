package dev.tigr.ares.fabric.event.movement;

import dev.tigr.simpleevents.event.Event;

public class ElytraMoveEvent extends Event {
    public double x;
    public double y;
    public double z;

    public ElytraMoveEvent(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }
}