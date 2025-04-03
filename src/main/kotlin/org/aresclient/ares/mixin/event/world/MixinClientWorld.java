package org.aresclient.ares.mixin.event.world;

import net.minecraft.client.world.ClientWorld;
import org.aresclient.ares.api.JWrapper;
import org.aresclient.ares.api.events.Era;
import org.aresclient.ares.api.events.TickEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientWorld.class)
public abstract class MixinClientWorld implements JWrapper {
    @Inject(method = "tick", at = @At("HEAD"))
    public void preTick(CallbackInfo ci) {
        EVENTS.post(new TickEvent.World(Era.BEFORE));
    }

    @Inject(method = "tick", at = @At("RETURN"))
    public void postTick(CallbackInfo ci) {
        EVENTS.post(new TickEvent.World(Era.AFTER));
    }
}
