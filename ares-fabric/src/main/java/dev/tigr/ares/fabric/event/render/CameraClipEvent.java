package dev.tigr.ares.fabric.event.render;

import dev.tigr.simpleevents.event.Event;

public class CameraClipEvent extends Event {
    double desiredCameraDistance;

    public CameraClipEvent(double desiredCameraDistance) {
        this.desiredCameraDistance = desiredCameraDistance;
    }

    public double getDesiredCameraDistance() {
        return desiredCameraDistance;
    }

    public void setDesiredCameraDistance(double desiredCameraDistance) {
        this.desiredCameraDistance = desiredCameraDistance;
    }
}
