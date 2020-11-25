package dev.tigr.ares.forge.mixin.client;

import dev.tigr.ares.core.Ares;
import dev.tigr.ares.forge.event.events.movement.WaterCollisionBoxEvent;
import dev.tigr.ares.forge.event.events.player.CanHandCollideWaterEvent;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * @author Tigermouthbear
 */
@Mixin(BlockLiquid.class)
public class MixinBlockLiquid {
    @Inject(method = "canCollideCheck", at = @At(value = "HEAD"), cancellable = true)
    public void canCollideCheck(IBlockState state, boolean hitIfLiquid, CallbackInfoReturnable<Boolean> cir) {
        Ares.EVENT_MANAGER.post(new CanHandCollideWaterEvent(cir));
    }

    @Inject(method = "getCollisionBoundingBox", at = @At("HEAD"), cancellable = true)
    public void getCollisionBoundingBox(IBlockState blockState, IBlockAccess blockAccess, BlockPos pos, final CallbackInfoReturnable<AxisAlignedBB> cir) {
        WaterCollisionBoxEvent event = Ares.EVENT_MANAGER.post(new WaterCollisionBoxEvent(pos));
        if(event.getBoundingBox() != null) {
            cir.setReturnValue(event.getBoundingBox());
            cir.cancel();
        }
    }
}
