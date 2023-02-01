package org.aresclient.ares.mixins;

import net.minecraft.client.Keyboard;
import net.minecraft.client.MinecraftClient;
import org.aresclient.ares.Ares;
import org.aresclient.ares.InputEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Keyboard.class)
public class MixinKeyboard {
    @Inject(method = "onKey", at = @At("HEAD"))
    public void onKey(long window, int key, int scancode, int action, int modifiers, CallbackInfo ci) {
        if(window == MinecraftClient.getInstance().getWindow().getHandle()) {
            if(action == 0) {
                Ares.Companion.getEVENT_MANAGER().post(new InputEvent.Keyboard.Released(key));
            } else {
                Ares.Companion.getEVENT_MANAGER().post(new InputEvent.Keyboard.Pressed(key));
            }
        }
    }
}