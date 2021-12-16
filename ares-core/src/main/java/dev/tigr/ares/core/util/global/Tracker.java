package dev.tigr.ares.core.util.global;

import dev.tigr.ares.Wrapper;
import dev.tigr.ares.core.Ares;

import java.util.LinkedHashSet;

public class Tracker implements Wrapper {
    static LinkedHashSet<Tracker> trackers = new LinkedHashSet<>();

    public static LinkedHashSet<Tracker> getTrackers() {
        return trackers;
    }

    public static <T extends Tracker> T addTracker(T tracker) {
        trackers.add(tracker);
        return tracker;
    }

    public void registerTrackers() {
        Ares.EVENT_MANAGER.register(this);
    }
}
