package org.aresclient.ares.mixins;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.RunArgs;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.TitleScreen;
import org.aresclient.ares.Ares;
import org.aresclient.ares.ScreenOpenedEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftClient.class)
public class MixinMinecraftClient {
    @Inject(method = "<init>", at = @At("RETURN"))
    public void init(RunArgs args, CallbackInfo ci) {
        Ares.Companion.getLOGGER().info("MinecraftClient <init> finished");
    }

    @Inject(method = "tick", at = @At("HEAD"))
    public void onTick(CallbackInfo ci) {
        Ares.Companion.clientTick();
    }

    @Inject(method = "setScreen", at = @At("RETURN"))
    public void postSetScreen(Screen screen, CallbackInfo ci) {
        Ares.Companion.getEVENT_MANAGER().post(new ScreenOpenedEvent(screen instanceof TitleScreen));
    }
}
