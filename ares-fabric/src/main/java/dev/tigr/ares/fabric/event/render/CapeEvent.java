package dev.tigr.ares.fabric.event.render;

import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

public class CapeEvent {
    private final PlayerListEntry playerListEntry;
    private final CallbackInfoReturnable<Identifier> cir;

    public CapeEvent(PlayerListEntry playerListEntry, CallbackInfoReturnable<Identifier> cir) {
        this.playerListEntry = playerListEntry;
        this.cir = cir;
    }

    public PlayerListEntry getPlayerInfo() {
        return playerListEntry;
    }

    public CallbackInfoReturnable<Identifier> getCir() {
        return cir;
    }
}
