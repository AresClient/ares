package org.aresclient.ares.mixin.event.entity;

import net.minecraft.client.network.ClientPlayerEntity;
import org.aresclient.ares.api.JWrapper;
import org.aresclient.ares.api.events.Era;
import org.aresclient.ares.api.events.TickEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayerEntity.class)
public class MixinClientPlayerEntity implements JWrapper {
    @Inject(method = "tickMovement", at = @At("HEAD"))
    public void preMotion(CallbackInfo ci) {
        EVENTS.post(new TickEvent.Motion(Era.BEFORE));
    }

    @Inject(method = "tickMovement", at = @At("RETURN"))
    public void postMotion(CallbackInfo ci) {
        EVENTS.post(new TickEvent.Motion(Era.AFTER));
    }
}
