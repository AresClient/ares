package org.aresclient.ares.mixin.event.client;

import net.minecraft.client.Keyboard;
import net.minecraft.client.MinecraftClient;
import org.aresclient.ares.api.JWrapper;
import org.aresclient.ares.api.events.InputEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Keyboard.class)
public class MixinKeyboard implements JWrapper {
    @Inject(method = "onKey", at = @At("HEAD"))
    public void onKey(long window, int key, int scancode, int action, int modifiers, CallbackInfo ci) {
        if(window == MinecraftClient.getInstance().getWindow().getHandle()) {
            if(action == 0) {
                EVENTS.post(new InputEvent.Keyboard.Released(key));
            } else {
                EVENTS.post(new InputEvent.Keyboard.Pressed(key));
            }
        }
    }
}
