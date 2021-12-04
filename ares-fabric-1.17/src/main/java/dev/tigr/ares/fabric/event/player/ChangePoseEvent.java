package dev.tigr.ares.fabric.event.player;

import dev.tigr.simpleevents.event.Event;
import net.minecraft.entity.EntityPose;

public class ChangePoseEvent extends Event {
    private EntityPose pose;

    public ChangePoseEvent(EntityPose pose) {
        this.pose = pose;
    }

    public EntityPose getPose() {
        return pose;
    }

    public void setPose(EntityPose pose) {
        this.pose = pose;
    }
}
