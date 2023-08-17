package org.aresclient.ares.api.event;

import dev.tigr.simpleevents.event.Event;

public class AresEvent extends Event {
    // when the event is posted in relation to the action
    public enum Era {
        BEFORE,
        AFTER
    }

    private final String name;
    private final Era era; // nullable

    public AresEvent(String name, Era era) {
        this.name = name;
        this.era = era;
    }

    public AresEvent(String name) {
        this(name, null);
    }

    public String getName() {
        return name;
    }

    public Era getEra() {
        return era;
    }
}
