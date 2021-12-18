package dev.tigr.ares.core.event.player;

import dev.tigr.simpleevents.event.Event;

public class ChangePoseEvent extends Event {
    private String pose;

    public ChangePoseEvent(String pose) {
        this.pose = pose;
    }

    public String getPose() {
        return pose;
    }

    public void setPose(String pose) {
        this.pose = pose;
    }
}
