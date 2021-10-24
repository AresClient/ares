package dev.tigr.ares.fabric.mixin.render;

import dev.tigr.ares.core.Ares;
import dev.tigr.ares.fabric.event.render.CameraClipEvent;
import net.minecraft.client.render.Camera;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * @author Tigermouthbear 8/30/20
 */
@Mixin(Camera.class)
public class MixinCamera {
    @Inject(method = "clipToSpace", at = @At("HEAD"), cancellable = true)
    public void clipCamera(double desiredCameraDistance, CallbackInfoReturnable<Double> callbackInfoReturnable) {
        CameraClipEvent event = new CameraClipEvent(desiredCameraDistance);
        if(Ares.EVENT_MANAGER.post(event).isCancelled()) {
            callbackInfoReturnable.setReturnValue(event.getDesiredCameraDistance());
            callbackInfoReturnable.cancel();
        }
    }
}
