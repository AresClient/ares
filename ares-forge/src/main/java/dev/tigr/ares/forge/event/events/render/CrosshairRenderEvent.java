package dev.tigr.ares.forge.event.events.render;

import dev.tigr.simpleevents.event.Event;

public class CrosshairRenderEvent extends Event {
    private final float partialTicks;

    public CrosshairRenderEvent(float partialTicks) {
        this.partialTicks = partialTicks;
    }

    public float getPartialTicks() {
        return partialTicks;
    }
}
