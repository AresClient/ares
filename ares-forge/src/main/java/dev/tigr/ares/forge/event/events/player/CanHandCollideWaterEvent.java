package dev.tigr.ares.forge.event.events.player;

import dev.tigr.simpleevents.event.Event;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

public class CanHandCollideWaterEvent extends Event {
    private final CallbackInfoReturnable<Boolean> cir;

    public CanHandCollideWaterEvent(CallbackInfoReturnable<Boolean> cir) {
        this.cir = cir;
    }

    public CallbackInfoReturnable<Boolean> getCir() {
        return cir;
    }
}
