package dev.tigr.ares.forge.event.events.movement;

import dev.tigr.simpleevents.event.Event;
import net.minecraft.entity.MoverType;

public class MovePlayerEvent extends Event {
    private final MoverType moverType;
    private double x;
    private double y;
    private double z;
    private boolean shouldDo;

    public MovePlayerEvent(MoverType moverType, double x, double y, double z) {
        this.moverType = moverType;
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public MoverType getMoverType() {
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

    public void setShouldDo(boolean shouldDo) {
        this.shouldDo = shouldDo;
    }

    public boolean getShouldDo() {
        return shouldDo;
    }
}
