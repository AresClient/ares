package dev.tigr.ares.core.util.tracker;

import dev.tigr.ares.core.event.movement.SendMovementPacketsEvent;
import dev.tigr.ares.core.event.render.PlayerModelRenderEvent;
import dev.tigr.ares.core.util.Timer;
import dev.tigr.ares.core.util.global.Tracker;
import dev.tigr.ares.core.util.math.floats.V2F;
import dev.tigr.simpleevents.listener.EventHandler;
import dev.tigr.simpleevents.listener.EventListener;

import static dev.tigr.ares.core.feature.module.modules.player.Rotations.ROTATION_SETTINGS;

/**
 * @author Makrennel 2021/12/16 - Split from RotationManager Module
 */
public class RotationTracker extends Tracker {
    Timer rotationDelay = new Timer();
    V2F currentRotation = null;
    int currentPriority = -1;
    int key = -1;
    boolean completedAction;

    public boolean setCurrentRotation(float yaw, float pitch, int key, int priority, boolean instant, boolean instantBypassCurrent) {
        return setCurrentRotation(new V2F(yaw, pitch), priority, key, instant, instantBypassCurrent);
    }
    public boolean setCurrentRotation(V2F rotation, int key, int priority, boolean instant, boolean instantBypassCurrent) {
        if(currentRotation == null || currentPriority <= priority || this.key == key || this.key == -1 || completedAction) {
            //Rotate instantly if specified, but only if the rotation does not match (to try and reduce rubberbanding)
            if(instant && currentRotation != rotation) PACKET.playerRotation(rotation);

            currentRotation = rotation;
            currentPriority = priority;
            this.key = key;
            rotationDelay.reset();
            return true;
        }

        //If bypass current flagged, use instant rotation anyways and return true for placement (might not actually be useful)
        if(instant && instantBypassCurrent && currentRotation != rotation) {
            PACKET.playerRotation(rotation);
            return true;
        }
        return false;
    }

    public void setCompletedAction(int key, boolean completedAction) {
        //If action is complete another key with lower priority can take over and set a new rotation without having to wait for reset delay to pass
        if(this.key == key) {
            this.completedAction = completedAction;

            //Can use this to reset rotation reset delay
            if(!completedAction) rotationDelay.reset();
        }
    }

    public V2F getCurrentRotation() {
        return currentRotation;
    }

    public boolean isKeyCurrent(int key) {
        return this.key == key;
    }

    public boolean isCompletedAction() {
        return completedAction;
    }

    public int getCurrentPriority() {
        return currentPriority;
    }

    @EventHandler
    private final EventListener<SendMovementPacketsEvent.Pre> onMovementPacketSent = new EventListener<>(event -> {
        if(currentRotation == null) {
            rotationDelay.reset();
            return;
        }

        //If reset delay has passed reset everything
        if(rotationDelay.passedTicks(ROTATION_SETTINGS.resetDelay.getValue())) {
            currentRotation = null;
            currentPriority = -1;
            key = -1;
            completedAction = false;
            rotationDelay.reset();
            return;
        }

        event.setRotation(currentRotation);
        event.setCancelled(true);

        //Apply adjustments to Freecam model if necessary
//        if(Freecam.INSTANCE.getEnabled()) {
//            Freecam.INSTANCE.clone.headYaw = currentRotation.x;
//            Freecam.INSTANCE.clone.bodyYaw = currentRotation.x;
//            Freecam.INSTANCE.clone.setYaw(currentRotation.x);
//            Freecam.INSTANCE.clone.setPitch(currentRotation.y);
//        }
    });

    @EventHandler
    public final EventListener<PlayerModelRenderEvent> onPlayerModelRender = new EventListener<>(event -> {
        if(!ENTITY.isSelf(event.getEntity()) || currentRotation == null) return;

        event.setCancelled(true);
        event.setHeadPitch(currentRotation.b);
        event.setHeadYaw(currentRotation.a);
        if(ROTATION_SETTINGS.renderBodyYaw.getValue()) event.setBodyYaw(currentRotation.a);
    });
}
