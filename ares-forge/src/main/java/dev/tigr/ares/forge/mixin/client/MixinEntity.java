package dev.tigr.ares.forge.mixin.client;

import dev.tigr.ares.core.Ares;
import dev.tigr.ares.forge.event.events.movement.EntityPushEvent;
import dev.tigr.ares.forge.event.events.movement.WalkOffLedgeEvent;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.Slice;

/**
 * @author Tigermouthbear
 */
@Mixin(Entity.class)
public abstract class MixinEntity {
    @Redirect(method = "move", slice = @Slice(from = @At(value = "FIELD", target = "Lnet/minecraft/entity/Entity;onGround:Z", ordinal = 0)), at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;isSneaking()Z", ordinal = 0))
    private boolean isSneakingWrapper(Entity entity) {
        return Ares.EVENT_MANAGER.post(new WalkOffLedgeEvent(entity.isSneaking())).isSneaking;
    }

    @Redirect(method = "applyEntityCollision", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;addVelocity(DDD)V"))
    public void entityCollisionWrapper(Entity entity, double x, double y, double z) {
        if(!Ares.EVENT_MANAGER.post(new EntityPushEvent(entity, x, y, z)).isCancelled()) entity.addVelocity(x, y, z);
    }
}
