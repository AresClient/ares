package org.aresclient.ares.mixin.mixins;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Box;
import org.aresclient.ares.api.JWrapper;
import org.aresclient.ares.impl.instrument.module.modules.movement.SafeWalk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerEntity.class)
public class MixinPlayerEntity implements JWrapper {
    @Inject(method = "clipAtLedge", at = @At("HEAD"), cancellable = true)
    private void clipAtLedge(CallbackInfoReturnable<Boolean> cir) {
        if(SafeWalk.INSTANCE.isEnabled()) cir.setReturnValue(true);
    }

    @Redirect(method = "isSpaceAroundPlayerEmpty", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/PlayerEntity;getBoundingBox()Lnet/minecraft/util/math/Box;"))
    private Box getClipBoundingBox(PlayerEntity player) {
        if(SafeWalk.INSTANCE.isEnabled() && (Object) this == MC.player) {
            double tolerance = SafeWalk.INSTANCE.getTolerance();
            double offset = tolerance * 0.5;
            return player.getBoundingBox().shrink(tolerance, 0.0, tolerance).offset(offset, 0, offset);
        }
        return player.getBoundingBox();
    }
}
