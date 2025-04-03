package org.aresclient.ares.mixin.event.client;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.TitleScreen;
import org.aresclient.ares.api.JWrapper;
import org.aresclient.ares.api.events.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftClient.class)
public class MixinMinecraftClient implements JWrapper {
    @Inject(method = "tick", at = @At("HEAD"))
    public void preTick(CallbackInfo ci) {
        EVENTS.post(new TickEvent.Client(Era.BEFORE));
    }

    @Inject(method = "tick", at = @At("RETURN"))
    public void postTick(CallbackInfo ci) {
        EVENTS.post(new TickEvent.Client(Era.AFTER));
    }

    @Inject(method = "render", at = @At("HEAD"))
    public void preGameLoop(boolean tick, CallbackInfo ci) {
        EVENTS.post(new TickEvent.GameLoop(Era.BEFORE));
    }

    @Inject(method = "render", at = @At("RETURN"))
    public void postGameLoop(boolean tick, CallbackInfo ci) {
        EVENTS.post(new TickEvent.GameLoop(Era.AFTER));
    }

    @Inject(method = "setScreen", at = @At("RETURN"))
    public void postSetScreen(Screen screen, CallbackInfo ci) {
        EVENTS.post(new ScreenOpenedEvent(screen instanceof TitleScreen));
    }

    @Inject(method = "stop", at = @At("HEAD"))
    public void preStop(CallbackInfo ci) {
        EVENTS.post(new ShutdownEvent());
    }
}
