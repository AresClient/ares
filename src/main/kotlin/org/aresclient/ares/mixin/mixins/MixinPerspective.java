package org.aresclient.ares.mixin.mixins;

import net.minecraft.client.option.Perspective;
import org.aresclient.ares.impl.instrument.modules.render.CameraClip;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Perspective.class)
public class MixinPerspective {
    @Inject(method = "next", at = @At("RETURN"), cancellable = true)
    private void next(CallbackInfoReturnable<Perspective> cir) {
        if(CameraClip.INSTANCE.shouldSkipFront() && cir.getReturnValue() == Perspective.THIRD_PERSON_FRONT)
            cir.setReturnValue(Perspective.FIRST_PERSON);
    }
}
