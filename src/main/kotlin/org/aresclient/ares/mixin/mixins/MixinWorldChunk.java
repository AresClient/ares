package org.aresclient.ares.mixin.mixins;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.chunk.WorldChunk;
import org.aresclient.ares.api.JWrapper;
import org.aresclient.ares.api.events.BlockStateUpdateEvent;
import org.aresclient.ares.api.events.BlockEntityEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Map;

@Mixin(WorldChunk.class)
public abstract class MixinWorldChunk implements JWrapper {
	@Shadow public abstract Map<BlockPos, BlockEntity> getBlockEntities();

	@Inject(method = "setBlockState", at = @At("RETURN"))
	private void onBlockStateUpdate(BlockPos pos, BlockState state, int flags, CallbackInfoReturnable<BlockState> cir) {
		EVENTS.post(new BlockStateUpdateEvent(pos, state, cir.getReturnValue()));
	}

	@Inject(method = "setBlockEntity", at = @At("TAIL"))
	private void onAddBlockEntity(BlockEntity blockEntity, CallbackInfo ci) {
		EVENTS.post(new BlockEntityEvent.Add(blockEntity));
	}

	@Inject(method = "removeBlockEntity", at = @At("HEAD"))
	private void onRemoveBlockEntity(BlockPos pos, CallbackInfo ci) {
		EVENTS.post(new BlockEntityEvent.Remove(getBlockEntities().get(pos)));
	}
}
