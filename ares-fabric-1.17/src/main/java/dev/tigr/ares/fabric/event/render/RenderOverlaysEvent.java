package dev.tigr.ares.fabric.event.render;

import dev.tigr.simpleevents.event.Event;

public class RenderOverlaysEvent extends Event {
    public enum Type { FIRE, WATER, BLOCK }
    private final Type type;

    public RenderOverlaysEvent(Type type) {
        this.type = type;
    }

    public Type getType() {
        return type;
    }
}
