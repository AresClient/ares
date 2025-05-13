package org.aresclient.ares.mixin.mixins;

import net.minecraft.client.input.KeyboardInput;
import org.aresclient.ares.api.JWrapper;
import org.aresclient.ares.api.events.Era;
import org.aresclient.ares.api.events.KeyboardInputEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(KeyboardInput.class)
public class MixinKeyboardInput implements JWrapper {
    @Inject(method = "tick", at = @At("HEAD"))
    private void preTickKeyboardInput(CallbackInfo ci) {
        EVENTS.post(new KeyboardInputEvent(Era.BEFORE));
    }

    @Inject(method = "tick", at = @At("TAIL"))
    private void postTickKeyboardInput(CallbackInfo ci) {
        EVENTS.post(new KeyboardInputEvent(Era.AFTER));
    }
}
