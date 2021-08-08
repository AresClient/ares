package dev.tigr.ares.forge.event.events.render;

import dev.tigr.simpleevents.event.Event;

public class GammaEvent extends Event {
    private float gamma;

    public GammaEvent(float gamma) {
        this.gamma = gamma;
    }

    public float getGamma() {
        return gamma;
    }

    public void setGamma(float gamma) {
        this.gamma = gamma;
    }
}
