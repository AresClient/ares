package dev.tigr.ares.forge.mixin.client;

import dev.tigr.ares.core.Ares;
import dev.tigr.ares.forge.event.events.client.EntityEvent;
import net.minecraft.entity.Entity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * @author Tigermouthbear
 */
@Mixin(World.class)
public class MixinWorld {
    @Inject(method = "spawnEntity", at = @At("RETURN"))
    public void spawnEntity(Entity entityIn, CallbackInfoReturnable<Boolean> cir) {
        Ares.EVENT_MANAGER.post(new EntityEvent.Spawn(entityIn));
    }

    @Inject(method = "removeEntity", at = @At("HEAD"))
    public void removeEntity(Entity entityIn, CallbackInfo ci) {
        Ares.EVENT_MANAGER.post(new EntityEvent.Remove(entityIn));
    }
}
