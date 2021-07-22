package dev.tigr.ares.fabric.mixin.client;

import dev.tigr.ares.core.Ares;
import dev.tigr.ares.fabric.event.movement.WalkOffLedgeEvent;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * @author Tigermouthbear 10/3/20
 */
@Mixin(PlayerEntity.class)
public class MixinPlayerEntity {
    PlayerEntity playerEntity = (PlayerEntity) ((Object) this);

    @Inject(method = "clipAtLedge", at = @At("HEAD"), cancellable = true)
    public void isSneakingWrapper(CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(Ares.EVENT_MANAGER.post(new WalkOffLedgeEvent(playerEntity.isSneaking())).isSneaking);
        cir.cancel();
    }
}
