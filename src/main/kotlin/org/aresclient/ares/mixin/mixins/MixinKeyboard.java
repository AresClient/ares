package org.aresclient.ares.mixin.mixins;

import it.unimi.dsi.fastutil.ints.IntArraySet;
import net.minecraft.client.Keyboard;
import net.minecraft.client.MinecraftClient;
import org.aresclient.ares.api.JWrapper;
import org.aresclient.ares.api.events.CharTypedEvent;
import org.aresclient.ares.api.events.InputEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Keyboard.class)
public class MixinKeyboard implements JWrapper {
    @Unique private static IntArraySet pressed = new IntArraySet();

    @Inject(method = "onKey", at = @At("HEAD"), cancellable = true)
    public void onKey(long window, int key, int scancode, int action, int modifiers, CallbackInfo ci) {
        if(window == MinecraftClient.getInstance().getWindow().getHandle()) {
            if(action == 0) {
                if (EVENTS.post(new InputEvent.Keyboard.Released(key)).isCancelled()) ci.cancel();
                pressed.remove(key);
            } else {
                var isRepeat = pressed.contains(key);
                if (EVENTS.post(new InputEvent.Keyboard.Pressed(key, isRepeat)).isCancelled()) ci.cancel();
                if (!isRepeat) pressed.add(key);
            }
        }
    }

    @Inject(method = "onChar", at = @At(value = "HEAD"))
    public void onChar(long window, int codePoint, int modifiers, CallbackInfo ci) {
        EVENTS.post(new CharTypedEvent(codePoint, modifiers));
    }
}
