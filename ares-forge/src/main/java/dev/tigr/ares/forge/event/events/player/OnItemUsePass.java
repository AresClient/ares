package dev.tigr.ares.forge.event.events.player;

import dev.tigr.simpleevents.event.Event;
import net.minecraft.util.EnumActionResult;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

public class OnItemUsePass extends Event {
    private final CallbackInfoReturnable<EnumActionResult> cir;

    public OnItemUsePass(CallbackInfoReturnable<EnumActionResult> cir) {
        this.cir = cir;
    }

    public CallbackInfoReturnable<EnumActionResult> getCir() {
        return cir;
    }
}
