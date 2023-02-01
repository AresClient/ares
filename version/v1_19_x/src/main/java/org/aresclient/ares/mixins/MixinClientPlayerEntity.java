package org.aresclient.ares.mixins;

import net.minecraft.client.network.ClientPlayerEntity;
import org.aresclient.ares.Ares;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayerEntity.class)
public class MixinClientPlayerEntity {
    @Inject(method = "tickMovement", at = @At("HEAD"))
    public void onMotion(CallbackInfo ci) {
        Ares.Companion.motionTick();
    }
}
