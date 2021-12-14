package dev.tigr.ares.core.event.movement;

import dev.tigr.ares.core.util.math.doubles.V2D;
import dev.tigr.simpleevents.event.Event;

import javax.annotation.Nonnull;

public class MovePlayerEvent extends Event {
    private final String moverType;
    private double x;
    private double y;
    private double z;

    public MovePlayerEvent(@Nonnull String moverType, double x, double y, double z) {
        this.moverType = moverType;
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public String getMoverType() {
        return moverType;
    }

    public double getX() {
        return x;
    }

    public MovePlayerEvent setX(double x) {
        this.x = x;
        return this;
    }

    public double getY() {
        return y;
    }

    public MovePlayerEvent setY(double y) {
        this.y = y;
        return this;
    }

    public double getZ() {
        return z;
    }

    public MovePlayerEvent setZ(double z) {
        this.z = z;
        return this;
    }

    public MovePlayerEvent set(V2D xz) {
        this.x = xz.getA();
        this.z = xz.getB();
        return this;
    }

    public void set(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }
}
