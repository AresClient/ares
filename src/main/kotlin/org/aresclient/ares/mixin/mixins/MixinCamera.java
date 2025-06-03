package org.aresclient.ares.mixin.mixins;

import net.minecraft.client.render.Camera;
import net.minecraft.entity.Entity;
import net.minecraft.world.BlockView;
import org.aresclient.ares.api.JWrapper;
import org.aresclient.ares.api.events.CameraEvent;
import org.aresclient.ares.impl.instrument.modules.render.CameraClip;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

@Mixin(Camera.class)
public class MixinCamera implements JWrapper {
	@Shadow private boolean thirdPerson;
	@Shadow private float lastTickProgress;

	@Inject(method = "update", at = @At(value = "RETURN"))
	private void onUpdateThirdPerson(BlockView area, Entity focusedEntity, boolean thirdPerson, boolean inverseView, float tickProgress, CallbackInfo ci) {
		var b = org.aresclient.ares.impl.instrument.globals.Camera.INSTANCE.shouldRenderCharacter();
		if (b == null) return;
		this.thirdPerson = b;
	}

	@ModifyArgs(method = "update", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/Camera;setPos(DDD)V"))
	private void onUpdatePosition(Args args) {
		var event = EVENTS.post(new CameraEvent.Position(lastTickProgress, args.get(0), args.get(1), args.get(2)));

		if (!event.isCancelled()) return;

		args.set(0, event.getX());
		args.set(1, event.getY());
		args.set(2, event.getZ());
	}

	@ModifyArgs(method = "update", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/Camera;setRotation(FF)V"))
	private void onUpdateRotation(Args args) {
		var event = EVENTS.post(new CameraEvent.Rotation(lastTickProgress, args.get(0), args.get(1)));

		if (!event.isCancelled()) return;

		args.set(0, event.getYaw());
		args.set(1, event.getPitch());
	}

	@Inject(method = "clipToSpace", at = @At("HEAD"), cancellable = true)
	private void clipToSpace(float f, CallbackInfoReturnable<Float> cir) {
		if(CameraClip.INSTANCE.isEnabled()) {
			if(CameraClip.INSTANCE.shouldModifyDistance()) cir.setReturnValue(CameraClip.INSTANCE.getDistance());
			else cir.setReturnValue(f);
			cir.cancel();
		}
	}
}
