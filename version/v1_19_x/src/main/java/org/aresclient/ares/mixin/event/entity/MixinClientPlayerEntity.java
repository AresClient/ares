package org.aresclient.ares.mixin.event.entity;

import net.minecraft.client.network.ClientPlayerEntity;
import org.aresclient.ares.api.Ares;
import org.aresclient.ares.api.event.AresEvent;
import org.aresclient.ares.api.event.client.TickEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayerEntity.class)
public class MixinClientPlayerEntity {
    @Inject(method = "tickMovement", at = @At("HEAD"))
    public void preMotion(CallbackInfo ci) {
        Ares.getEventManager().post(new TickEvent.Motion(AresEvent.Era.BEFORE));
    }

    @Inject(method = "tickMovement", at = @At("RETURN"))
    public void postMotion(CallbackInfo ci) {
        Ares.getEventManager().post(new TickEvent.Motion(AresEvent.Era.AFTER));
    }
}
