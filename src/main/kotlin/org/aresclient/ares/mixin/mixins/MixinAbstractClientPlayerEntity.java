package org.aresclient.ares.mixin.mixins;

import net.minecraft.client.network.AbstractClientPlayerEntity;
import org.aresclient.ares.impl.instrument.modules.render.NoRender;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractClientPlayerEntity.class)
public class MixinAbstractClientPlayerEntity {
    @Inject(method = "getFovMultiplier", at = @At("HEAD"), cancellable = true)
    private void getFovMultiplier(CallbackInfoReturnable<Float> cir) {
        if(NoRender.INSTANCE.shouldBlockFovChange()) cir.setReturnValue(1f);
    }
}
