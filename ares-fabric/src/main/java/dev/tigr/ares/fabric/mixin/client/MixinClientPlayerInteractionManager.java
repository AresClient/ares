package dev.tigr.ares.fabric.mixin.client;

import dev.tigr.ares.core.Ares;
import dev.tigr.ares.fabric.event.client.ResetBlockRemovingEvent;
import dev.tigr.ares.fabric.event.player.DamageBlockEvent;
import dev.tigr.ares.fabric.event.player.DestroyBlockEvent;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * @author Tigermouthbear
 */
@Mixin(ClientPlayerInteractionManager.class)
public class MixinClientPlayerInteractionManager {
    @Inject(method = "cancelBlockBreaking", at = @At("HEAD"), cancellable = true)
    private void resetBlockWrapper(CallbackInfo ci) {
        ResetBlockRemovingEvent event = Ares.EVENT_MANAGER.post(new ResetBlockRemovingEvent());
        if(Ares.EVENT_MANAGER.post(new ResetBlockRemovingEvent()).isCancelled()) ci.cancel();
    }

    @Inject(method = "updateBlockBreakingProgress", at = @At("HEAD"), cancellable = true)
    private void damageBlock(BlockPos blockPos, Direction direction, CallbackInfoReturnable<Boolean> cir) {
        if(Ares.EVENT_MANAGER.post(new DamageBlockEvent(blockPos, direction)).isCancelled()) cir.cancel();
    }

    @Inject(method = "breakBlock", at = @At("RETURN"))
    public void breakBlock(BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        Ares.EVENT_MANAGER.post(new DestroyBlockEvent(pos));
    }
}
