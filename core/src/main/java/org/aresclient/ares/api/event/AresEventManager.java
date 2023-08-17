package org.aresclient.ares.api.event;

import dev.tigr.simpleevents.EventManager;
import org.aresclient.ares.api.Ares;
import org.aresclient.ares.api.minecraft.util.Profiler;

public class AresEventManager extends EventManager {
    @Override
    public <T> T post(T event) {
        if(event instanceof AresEvent) {
            AresEvent aresEvent = (AresEvent) event;
            Profiler profiler = Ares.getMinecraft().getProfiler();
            profiler.push("ares_" + aresEvent.getName() + (aresEvent.getEra() != null ? "_" + aresEvent.getEra().name().toLowerCase() : ""));
            T out = super.post(event);
            profiler.pop();
            return out;
        } else return super.post(event);
    }
}
