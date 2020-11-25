package dev.tigr.ares.forge.event.events.optimizations;

import dev.tigr.simpleevents.event.Event;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.util.ResourceLocation;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

public class CapeEvent extends Event {
    private final NetworkPlayerInfo playerInfo;
    private final CallbackInfoReturnable<ResourceLocation> cir;

    public CapeEvent(NetworkPlayerInfo playerInfo, CallbackInfoReturnable<ResourceLocation> cir) {
        this.playerInfo = playerInfo;
        this.cir = cir;
    }

    public NetworkPlayerInfo getPlayerInfo() {
        return playerInfo;
    }

    public CallbackInfoReturnable<ResourceLocation> getCir() {
        return cir;
    }
}
