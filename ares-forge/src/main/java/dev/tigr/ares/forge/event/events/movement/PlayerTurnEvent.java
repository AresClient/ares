package dev.tigr.ares.forge.event.events.movement;

import dev.tigr.simpleevents.event.Event;

public class PlayerTurnEvent extends Event {
    private float pitch;
    private float yaw;

    public PlayerTurnEvent(float pitch, float yaw) {
        this.pitch = pitch;
        this.yaw = yaw;
    }

    public float getPitch() {
        return pitch;
    }

    public void setPitch(float pitch) {
        this.pitch = pitch;
    }

    public float getYaw() {
        return yaw;
    }

    public void setYaw(float yaw) {
        this.yaw = yaw;
    }
}
