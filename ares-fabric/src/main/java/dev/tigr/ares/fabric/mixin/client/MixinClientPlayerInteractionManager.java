package dev.tigr.ares.fabric.mixin.client;

import dev.tigr.ares.core.Ares;
import dev.tigr.ares.fabric.event.client.ResetBlockRemovingEvent;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

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
}
