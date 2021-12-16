package dev.tigr.ares.core.event.render;

import dev.tigr.simpleevents.event.Event;

public class PlayerModelRenderEvent extends Event {
    int entity;
    float headPitch, headYaw, bodyYaw;

    public PlayerModelRenderEvent(int entity, float headPitch, float headYaw, float bodyYaw) {
        this.entity = entity;
        this.headPitch = headPitch;
        this.headYaw = headYaw;
        this.bodyYaw = bodyYaw;
    }

    public int getEntity() {
        return entity;
    }

    public float getHeadPitch() {
        return headPitch;
    }

    public float getHeadYaw() {
        return headYaw;
    }

    public float getBodyYaw() {
        return bodyYaw;
    }

    public void setHeadPitch(float headPitch) {
        this.headPitch = headPitch;
    }

    public void setHeadYaw(float headYaw) {
        this.headYaw = headYaw;
    }

    public void setBodyYaw(float bodyYaw) {
        this.bodyYaw = bodyYaw;
    }
}
