package org.aresclient.ares.mixin.event.client;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.RunArgs;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.TitleScreen;
import org.apache.logging.log4j.LogManager;
import org.aresclient.ares.api.Ares;
import org.aresclient.ares.api.event.AresEvent;
import org.aresclient.ares.api.event.client.ScreenOpenedEvent;
import org.aresclient.ares.api.event.client.ShutdownEvent;
import org.aresclient.ares.api.event.client.TickEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftClient.class)
public class MixinMinecraftClient {
    @Inject(method = "tick", at = @At("HEAD"))
    public void preTick(CallbackInfo ci) {
        Ares.getEventManager().post(new TickEvent.Client(AresEvent.Era.BEFORE));
    }

    @Inject(method = "tick", at = @At("RETURN"))
    public void postTick(CallbackInfo ci) {
        Ares.getEventManager().post(new TickEvent.Client(AresEvent.Era.AFTER));
    }

    @Inject(method = "render", at = @At("HEAD"))
    public void preGameLoop(boolean tick, CallbackInfo ci) {
        Ares.getEventManager().post(new TickEvent.GameLoop(AresEvent.Era.BEFORE));
    }

    @Inject(method = "render", at = @At("RETURN"))
    public void postGameLoop(boolean tick, CallbackInfo ci) {
        Ares.getEventManager().post(new TickEvent.GameLoop(AresEvent.Era.AFTER));
    }

    @Inject(method = "setScreen", at = @At("RETURN"))
    public void postSetScreen(Screen screen, CallbackInfo ci) {
        Ares.getEventManager().post(new ScreenOpenedEvent(screen instanceof TitleScreen));
    }

    @Inject(method = "stop", at = @At("HEAD"))
    public void preStop(CallbackInfo ci) {
        Ares.getEventManager().post(new ShutdownEvent());
    }
}
