package org.aresclient.ares.mixin.mixins;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.block.BlockState;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.aresclient.ares.impl.instrument.modules.player.Sync;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ClientPlayerInteractionManager.class)
public class MixinClientPlayerInteractionManager {
    @WrapOperation(method = "breakBlock", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;setBlockState(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;I)Z"))
    private boolean onBreakBlockSetBlockState(World instance, BlockPos pos, BlockState state, int flags, Operation<Boolean> original) {
        if(Sync.INSTANCE.getGhostBlocks()) {
            Sync.INSTANCE.getRemovedBlocksSet().add(pos);
            return true;
        }
        else return original.call(instance, pos, state, flags);
    }
    
    @Inject(method = "attackBlock", at = @At("HEAD"), cancellable = true)
    private void onAttackBlock(BlockPos pos, Direction direction, CallbackInfoReturnable<Boolean> cir) {
        if(Sync.INSTANCE.getGhostBlocks() && Sync.INSTANCE.getRemovedBlocksSet().contains(pos)) {
            cir.setReturnValue(false);
            cir.cancel();
        }
    }

    @Inject(method = "updateBlockBreakingProgress", at = @At("HEAD"), cancellable = true)
    private void onUpdateBlockBreakingProgress(BlockPos pos, Direction direction, CallbackInfoReturnable<Boolean> cir) {
        if(Sync.INSTANCE.getGhostBlocks() && Sync.INSTANCE.getRemovedBlocksSet().contains(pos)) {
            cir.setReturnValue(false);
            cir.cancel();
        }
    }

}
