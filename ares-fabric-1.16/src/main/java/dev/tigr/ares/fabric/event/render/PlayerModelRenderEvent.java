package dev.tigr.ares.fabric.event.render;

import dev.tigr.simpleevents.event.Event;
import net.minecraft.entity.LivingEntity;

public class PlayerModelRenderEvent extends Event {
    LivingEntity livingEntity;
    float headPitch, headYaw, bodyYaw;

    public PlayerModelRenderEvent(LivingEntity livingEntity) {
        this.livingEntity = livingEntity;
        this.headPitch = livingEntity.pitch;
        this.headYaw = livingEntity.headYaw;
        this.bodyYaw = livingEntity.bodyYaw;
    }

    public LivingEntity getLivingEntity() {
        return livingEntity;
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
