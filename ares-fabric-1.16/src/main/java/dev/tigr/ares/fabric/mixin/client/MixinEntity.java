package dev.tigr.ares.fabric.mixin.client;

import dev.tigr.ares.core.Ares;
import dev.tigr.ares.fabric.event.movement.EntityClipEvent;
import dev.tigr.ares.fabric.event.movement.EntityPushEvent;
import dev.tigr.ares.fabric.event.movement.PlayerTurnEvent;
import dev.tigr.ares.fabric.event.movement.SlowDownEvent;
import dev.tigr.ares.fabric.event.player.ChangePoseEvent;
import dev.tigr.ares.fabric.mixin.accessors.EntityAccessor;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.MovementType;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * @author Tigermouthbear 9/26/20
 */
@Mixin(Entity.class)
public class MixinEntity {
    @Shadow private int entityId;
    private final Entity entity = ((Entity) (Object) this);

    @Inject(method = "changeLookDirection", at = @At("HEAD"), cancellable = true)
    public void changeLookDirection(CallbackInfo ci) {
        if(Ares.EVENT_MANAGER.post(new PlayerTurnEvent()).isCancelled()) ci.cancel();
    }

    @Inject(method = "move", at = @At("HEAD"), cancellable = true)
    public void move(MovementType movementType, Vec3d vec3d, CallbackInfo ci) {
        if(Ares.EVENT_MANAGER.post(new EntityClipEvent(entity)).isCancelled()) {
            entity.setBoundingBox(entity.getBoundingBox().offset(vec3d));
            entity.moveToBoundingBoxCenter();
            ci.cancel();
        }
    }

    @Inject(method = "isInsideWall", at = @At("HEAD"), cancellable = true)
    public void isInsideWall(CallbackInfoReturnable<Boolean> cir) {
        if(Ares.EVENT_MANAGER.post(new EntityClipEvent(entity)).isCancelled()) {
            cir.setReturnValue(false);
            cir.cancel();
        }
    }

    @Redirect(method = "pushAwayFrom", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;addVelocity(DDD)V", ordinal = 0))
    public void pushEntity(Entity entity, double x, double y, double z) {
        if(!Ares.EVENT_MANAGER.post(new EntityPushEvent(entity)).isCancelled()) entity.addVelocity(x, y, z);
    }

    @Inject(method = "getVelocityMultiplier", at = @At("HEAD"), cancellable = true)
    public void onGetVelocityMultiplier(CallbackInfoReturnable<Float> cir) {
        if(Ares.EVENT_MANAGER.post(new SlowDownEvent()).isCancelled()) {
            cir.setReturnValue(1.0f);
            cir.cancel();
        }
    }

    @Inject(method = "setPose", at = @At("HEAD"), cancellable = true)
    public void setPose(EntityPose pose, CallbackInfo ci) {
        ChangePoseEvent event = Ares.EVENT_MANAGER.post(new ChangePoseEvent(pose));
        if(event.getPose() != pose) {
            entity.getDataTracker().set(((EntityAccessor) entity).getPose(), event.getPose());
            ci.cancel();
        }
    }
}
