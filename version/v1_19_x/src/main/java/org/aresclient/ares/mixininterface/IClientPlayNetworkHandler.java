package org.aresclient.ares.mixininterface;

import net.minecraft.network.message.LastSeenMessagesCollector;

public interface IClientPlayNetworkHandler {
    LastSeenMessagesCollector getLastSeenMessagesCollector();
}
