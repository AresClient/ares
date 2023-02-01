package org.aresclient.ares.mixins;

import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.message.LastSeenMessagesCollector;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ClientPlayNetworkHandler.class)
public interface ClientPlayNetworkHandlerAccessor {
    @Accessor("lastSeenMessagesCollector")
    LastSeenMessagesCollector getLastSeenMessagesCollector();
}
