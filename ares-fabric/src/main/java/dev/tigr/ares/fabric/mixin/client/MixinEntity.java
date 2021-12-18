package dev.tigr.ares.fabric.mixin.client;

import dev.tigr.ares.Wrapper;
import dev.tigr.ares.core.Ares;
import dev.tigr.ares.core.event.movement.EntityClipEvent;
import dev.tigr.ares.fabric.event.movement.EntityPushEvent;
import dev.tigr.ares.fabric.event.movement.PlayerTurnEvent;
import dev.tigr.ares.fabric.event.movement.SlowDownEvent;
import dev.tigr.ares.fabric.event.player.CanHandCollideWaterEvent;
import dev.tigr.ares.core.event.player.ChangePoseEvent;
import dev.tigr.ares.fabric.mixin.accessors.EntityAccessor;
import dev.tigr.simpleevents.event.Result;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.MovementType;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Objects;

/**
 * @author Tigermouthbear 9/26/20
 */
@Mixin(Entity.class)
public abstract class MixinEntity implements Wrapper {
    @Shadow private int id;

    @Shadow public abstract Vec3d getCameraPosVec(float tickDelta);

    @Shadow public abstract Vec3d getRotationVec(float tickDelta);

    @Shadow public float prevPitch;
    private final Entity entity = ((Entity) (Object) this);

    @Inject(method = "changeLookDirection", at = @At("HEAD"), cancellable = true)
    public void changeLookDirection(CallbackInfo ci) {
        if(Ares.EVENT_MANAGER.post(new PlayerTurnEvent()).isCancelled()) ci.cancel();
    }

    @Inject(method = "move", at = @At("HEAD"), cancellable = true)
    public void move(MovementType movementType, Vec3d vec3d, CallbackInfo ci) {
        if(Ares.EVENT_MANAGER.post(new EntityClipEvent(entity.getId())).isCancelled()) {
            entity.setBoundingBox(entity.getBoundingBox().offset(vec3d));
            Box box = entity.getBoundingBox();
            entity.setPos((box.minX + box.maxX) / 2.0D, box.minY, (box.minZ + box.maxZ) / 2.0D);
            ci.cancel();
        }
    }

    @Inject(method = "isInsideWall", at = @At("HEAD"), cancellable = true)
    public void isInsideWall(CallbackInfoReturnable<Boolean> cir) {
        if(Ares.EVENT_MANAGER.post(new EntityClipEvent(entity.getId())).isCancelled()) {
            cir.setReturnValue(false);
            cir.cancel();
        }
    }

    @Inject(method = "pushAwayFrom", at = @At("HEAD"), cancellable = true)
    public void pushEntity(Entity entity, CallbackInfo ci) {
        if(Ares.EVENT_MANAGER.post(new EntityPushEvent()).isCancelled()) ci.cancel();
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
        ChangePoseEvent event = Ares.EVENT_MANAGER.post(new ChangePoseEvent(pose.name()));
        if(EntityPose.valueOf(event.getPose()) != pose) {
            entity.getDataTracker().set(((EntityAccessor) entity).getPose(), EntityPose.valueOf(event.getPose()));
            ci.cancel();
        }
    }

    // LiquidInteract - Credit - IUDevman
    @Inject(method = "raycast", at = @At("HEAD"), cancellable = true)
    public void liquidInteract(double maxDistance, float tickDelta, boolean includeFluids, CallbackInfoReturnable<HitResult> cir) {
        if(MC.player == null || MC.world == null || id != Objects.requireNonNull(MC.getCameraEntity()).getId() || MC.player.isSubmergedInWater()) return;

        if(Ares.EVENT_MANAGER.post(new CanHandCollideWaterEvent()).getResult() == Result.ALLOW) {
            Vec3d vec3d = getCameraPosVec(tickDelta);
            Vec3d vec3d2 = getRotationVec(tickDelta);
            Vec3d vec3d3 = vec3d.add(vec3d2.getX() * maxDistance, vec3d2.getY() * maxDistance, vec3d2.getZ() * maxDistance);
            cir.setReturnValue(MC.world.raycast(new RaycastContext(vec3d, vec3d3, RaycastContext.ShapeType.OUTLINE, RaycastContext.FluidHandling.ANY, MC.getCameraEntity())));
        }
    }
}
