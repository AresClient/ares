package org.aresclient.ares.api;

import dev.tigr.simpleevents.EventManager;
import net.minecraft.client.MinecraftClient;
import org.aresclient.ares.Ares;

public interface JWrapper {
    MinecraftClient MC = MinecraftClient.getInstance();
    EventManager EVENTS = Ares.getEVENT_MANAGER();
}
