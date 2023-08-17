package org.aresclient.ares.api.event.client;

import org.aresclient.ares.api.event.AresEvent;

public class ShutdownEvent extends AresEvent {
    public ShutdownEvent() {
        super("shutdown");
    }
}
