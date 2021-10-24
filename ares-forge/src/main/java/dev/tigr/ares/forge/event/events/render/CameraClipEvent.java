package dev.tigr.ares.forge.event.events.render;

import dev.tigr.simpleevents.event.Event;

public class CameraClipEvent extends Event {
    float tickDelta;

    public CameraClipEvent(float tickDelta) {
        this.tickDelta = tickDelta;
    }

    public float getTickDelta() {
        return tickDelta;
    }
}
