package org.aresclient.ares.mixin.mixins;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemPlacementContext;
import org.aresclient.ares.api.JWrapper;
import org.aresclient.ares.impl.instrument.modules.player.Sync;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BlockItem.class)
public abstract class MixinBlockItem implements JWrapper {
    @Inject(method = "place(Lnet/minecraft/item/ItemPlacementContext;Lnet/minecraft/block/BlockState;)Z", at = @At("HEAD"), cancellable = true)
    private void onPlace(ItemPlacementContext context, BlockState state, CallbackInfoReturnable<Boolean> cir) {
        if(!Sync.INSTANCE.getFalseBlocks() || MC.isInSingleplayer()) return;

        cir.setReturnValue(true);
        cir.cancel();
    }

    @WrapOperation(method = "place(Lnet/minecraft/item/ItemPlacementContext;)Lnet/minecraft/util/ActionResult;", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/BlockState;isOf(Lnet/minecraft/block/Block;)Z"))
    private boolean onPlaceIsBlockStateOf(BlockState instance, Block block, Operation<Boolean> original, @Local(ordinal = 0) BlockState placementState) {
        if(Sync.INSTANCE.getFalseBlocks() && !MC.isInSingleplayer()) return original.call(placementState, block);
        else return original.call(instance, block);
    }
}
