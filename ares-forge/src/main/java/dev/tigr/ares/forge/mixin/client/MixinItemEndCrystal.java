package dev.tigr.ares.forge.mixin.client;

import dev.tigr.ares.core.Ares;
import dev.tigr.ares.forge.event.events.player.PlaceCrystalEvent;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemEndCrystal;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * @author Tigermouthbear 7/14/20
 */
@Mixin(ItemEndCrystal.class)
public class MixinItemEndCrystal {
    @Inject(method = "onItemUse", at = @At("HEAD"), cancellable = true)
    public void onItemUse(EntityPlayer player, World worldIn, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ, CallbackInfoReturnable<EnumActionResult> cir) {
        if(Ares.EVENT_MANAGER.post(new PlaceCrystalEvent(pos)).isCancelled()) {
            cir.setReturnValue(EnumActionResult.PASS);
            cir.cancel();
        }
    }
}
