package dev.tigr.ares.fabric.event.render;

import dev.tigr.simpleevents.event.Event;

public class GammaEvent extends Event {
    private double gamma;

    public GammaEvent(double gamma) {
        this.gamma = gamma;
    }

    public double getGamma() {
        return gamma;
    }

    public void setGamma(double gamma) {
        this.gamma = gamma;
    }
}
