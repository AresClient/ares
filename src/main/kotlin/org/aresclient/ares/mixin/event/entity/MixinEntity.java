package org.aresclient.ares.mixin.event.entity;

import net.minecraft.entity.Entity;
import org.aresclient.ares.api.JWrapper;
import org.aresclient.ares.api.events.PlayerEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Entity.class)
public class MixinEntity implements JWrapper {
	@Inject(method = "changeLookDirection", at = @At("HEAD"), cancellable = true)
	private void onChangeLookDirection(double cursorDeltaX, double cursorDeltaY, CallbackInfo ci) {
		if ((Object) this != MC.player) return;

		if (EVENTS.post(new PlayerEvent.ChangeLookDirection(cursorDeltaX, cursorDeltaY)).isCancelled()) ci.cancel();
	}
}
