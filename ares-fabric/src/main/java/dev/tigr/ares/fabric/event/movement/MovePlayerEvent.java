package dev.tigr.ares.fabric.event.movement;

import dev.tigr.simpleevents.event.Event;
import net.minecraft.entity.MovementType;

public class MovePlayerEvent extends Event {
    private final MovementType moverType;
    private double x;
    private double y;
    private double z;

    public MovePlayerEvent(MovementType moverType, double x, double y, double z) {
        this.moverType = moverType;
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public MovementType getMoverType() {
        return moverType;
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

    public void set(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }
}
