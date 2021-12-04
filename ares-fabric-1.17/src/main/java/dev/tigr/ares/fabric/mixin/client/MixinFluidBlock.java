package dev.tigr.ares.fabric.mixin.client;

import dev.tigr.ares.core.Ares;
import dev.tigr.ares.fabric.event.movement.WaterCollisionEvent;
import net.minecraft.block.BlockState;
import net.minecraft.block.FluidBlock;
import net.minecraft.block.ShapeContext;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * @author Tigermouthbear 10/3/20
 */
@Mixin(FluidBlock.class)
public class MixinFluidBlock {
    @Inject(method = "getCollisionShape", at = @At("HEAD"), cancellable = true)
    public void getCollisionShape(BlockState blockState, BlockView blockView, BlockPos blockPos, ShapeContext shapeContext, CallbackInfoReturnable<VoxelShape> cir) {
        WaterCollisionEvent event = Ares.EVENT_MANAGER.post(new WaterCollisionEvent(blockPos));
        if(event.getShape() != null) {
            cir.setReturnValue(event.getShape());
            cir.cancel();
        }
    }
}
