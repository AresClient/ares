package org.aresclient.ares.api;

import dev.tigr.simpleevents.EventManager;
import net.minecraft.client.MinecraftClient;
import org.aresclient.ares.Main;
import org.aresclient.ares.api.events.AresEventManager;

public interface JWrapper {
	MinecraftClient MC = MinecraftClient.getInstance();
	EventManager EVENTS = Main.getEVENTS();
}
