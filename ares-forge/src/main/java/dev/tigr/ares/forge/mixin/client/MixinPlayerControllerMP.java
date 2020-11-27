package dev.tigr.ares.forge.mixin.client;

import dev.tigr.ares.core.Ares;
import dev.tigr.ares.forge.event.events.player.DamageBlockEvent;
import dev.tigr.ares.forge.event.events.player.DestroyBlockEvent;
import dev.tigr.ares.forge.event.events.player.ResetBlockRemovingEvent;
import net.minecraft.client.multiplayer.PlayerControllerMP;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * @author Tigermouthbear
 */
@Mixin(PlayerControllerMP.class)
public class MixinPlayerControllerMP {
    @Inject(method = "resetBlockRemoving", at = @At("HEAD"), cancellable = true)
    private void resetBlockWrapper(CallbackInfo ci) {
        if(Ares.EVENT_MANAGER.post(new ResetBlockRemovingEvent()).isCancelled()) ci.cancel();
    }

    @Inject(method = "onPlayerDestroyBlock", at = @At("RETURN"))
    private void destroyBlock(BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        Ares.EVENT_MANAGER.post(new DestroyBlockEvent(pos));
    }

    @Inject(method = "onPlayerDamageBlock", at = @At("HEAD"), cancellable = true)
    private void damageBlock(BlockPos blockPos, EnumFacing enumFacing, CallbackInfoReturnable<Boolean> cir) {
        if(Ares.EVENT_MANAGER.post(new DamageBlockEvent(blockPos, enumFacing)).isCancelled()) cir.cancel();
    }
}
