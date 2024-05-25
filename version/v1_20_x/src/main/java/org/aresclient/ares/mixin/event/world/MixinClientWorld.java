package org.aresclient.ares.mixin.event.world;

import net.minecraft.client.world.ClientWorld;
import org.aresclient.ares.api.Ares;
import org.aresclient.ares.api.event.AresEvent;
import org.aresclient.ares.api.event.client.TickEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientWorld.class)
public abstract class MixinClientWorld {
    @Inject(method = "tick", at = @At("HEAD"))
    public void preTick(CallbackInfo ci) {
        Ares.getEventManager().post(new TickEvent.World(AresEvent.Era.BEFORE));
    }

    @Inject(method = "tick", at = @At("RETURN"))
    public void postTick(CallbackInfo ci) {
        Ares.getEventManager().post(new TickEvent.World(AresEvent.Era.AFTER));
    }
}
