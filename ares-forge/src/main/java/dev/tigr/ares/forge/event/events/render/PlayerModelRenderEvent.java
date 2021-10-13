package dev.tigr.ares.forge.event.events.render;

import dev.tigr.simpleevents.event.Event;
import net.minecraft.entity.EntityLivingBase;

public class PlayerModelRenderEvent extends Event {
    EntityLivingBase livingEntity;
    float headPitch, headYaw, bodyYaw;

    public PlayerModelRenderEvent(EntityLivingBase livingEntity) {
        this.livingEntity = livingEntity;
        this.headPitch = livingEntity.rotationPitch;
        this.headYaw = livingEntity.rotationYawHead;
        this.bodyYaw = livingEntity.renderYawOffset;
    }

    public EntityLivingBase getLivingEntity() {
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
