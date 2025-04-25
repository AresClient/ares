package org.aresclient.ares.mixin.mixins;

import com.mojang.authlib.GameProfile;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.MovementType;
import net.minecraft.util.math.Vec3d;
import org.aresclient.ares.api.JWrapper;
import org.aresclient.ares.api.events.Era;
import org.aresclient.ares.api.events.PlayerEvent;
import org.aresclient.ares.api.events.TickEvent;
import org.aresclient.ares.impl.util.MathUtil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayerEntity.class)
public class MixinClientPlayerEntity extends AbstractClientPlayerEntity implements JWrapper {
    private MixinClientPlayerEntity(ClientWorld world, GameProfile profile) {
        super(world, profile);
    }

    @Inject(method = "tickMovement", at = @At("HEAD"))
    private void preMotion(CallbackInfo ci) {
        EVENTS.post(new TickEvent.Motion(Era.BEFORE));
    }

    @Inject(method = "tickMovement", at = @At("RETURN"))
    private void postMotion(CallbackInfo ci) {
        EVENTS.post(new TickEvent.Motion(Era.AFTER));
    }

    @Inject(method = "move", at = @At("HEAD"), cancellable = true)
    private void onMovePlayer(MovementType type, Vec3d movement, CallbackInfo ci) {
        var event = EVENTS.post(new PlayerEvent.Move(type, MathUtil.INSTANCE.duplicate(movement)));
        if (!event.isCancelled()) return;
        ci.cancel();
        super.move(type, event.getMovement());
    }
}
