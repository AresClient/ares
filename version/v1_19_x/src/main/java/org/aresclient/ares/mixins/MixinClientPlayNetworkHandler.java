package org.aresclient.ares.mixins;

import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.message.LastSeenMessagesCollector;
import org.aresclient.ares.mixininterface.IClientPlayNetworkHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(ClientPlayNetworkHandler.class)
public class MixinClientPlayNetworkHandler implements IClientPlayNetworkHandler {
    @Shadow private LastSeenMessagesCollector lastSeenMessagesCollector;

    @Override
    public LastSeenMessagesCollector getLastSeenMessagesCollector() {
        return lastSeenMessagesCollector;
    }
}
