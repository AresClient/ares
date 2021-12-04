package dev.tigr.ares.fabric.mixin.client;

import dev.tigr.ares.fabric.impl.modules.movement.HighJump;
import net.minecraft.block.Block;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * @author Tigermouthbear 8/30/20
 */
@Mixin(Block.class)
public class MixinBlock {
    @Inject(method = "getJumpVelocityMultiplier", at = @At("RETURN"), cancellable = true)
    public void jumpMultiplier(CallbackInfoReturnable<Float> callbackInfoReturnable) {
        callbackInfoReturnable.setReturnValue(callbackInfoReturnable.getReturnValue() * HighJump.getMultiplier());
    }
}
