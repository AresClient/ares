package org.aresclient.ares.mixin.mixins;

import net.minecraft.client.gui.screen.ChatScreen;
import org.aresclient.ares.api.JWrapper;
import org.aresclient.ares.api.events.ChatEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ChatScreen.class)
public class MixinChatScreen implements JWrapper {
    @Inject(method = "sendMessage", at = @At(value = "HEAD"), cancellable = true)
    private void sendMessage(String chatText, boolean addToHistory, CallbackInfo ci) {
        if(EVENTS.post(new ChatEvent(chatText)).isCancelled()) ci.cancel();
    }
}
