package dev.tigr.ares.forge.mixin.client;

import dev.tigr.ares.core.Ares;
import dev.tigr.ares.forge.event.events.movement.EntityPushEvent;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * @author Tigermouthbear
 */
@Mixin(Entity.class)
public abstract class MixinEntity {
    @Redirect(method = "applyEntityCollision", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;addVelocity(DDD)V"))
    public void entityCollisionWrapper(Entity entity, double x, double y, double z) {
        if(!Ares.EVENT_MANAGER.post(new EntityPushEvent(entity, x, y, z)).isCancelled()) entity.addVelocity(x, y, z);
    }
}
