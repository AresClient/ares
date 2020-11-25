package dev.tigr.ares.forge.mixin.client;

import dev.tigr.ares.core.Ares;
import dev.tigr.ares.forge.event.events.movement.BoatMoveEvent;
import net.minecraft.entity.MoverType;
import net.minecraft.entity.item.EntityBoat;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(EntityBoat.class)
public abstract class MixinEntityBoat {
    @Redirect(method = "onUpdate", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/item/EntityBoat;move(Lnet/minecraft/entity/MoverType;DDD)V"))
    public void onMove(EntityBoat entityBoat, MoverType type, double x, double y, double z) {
        BoatMoveEvent boatMoveEvent = Ares.EVENT_MANAGER.post(new BoatMoveEvent(entityBoat, x, y, z));
        if(!boatMoveEvent.isCancelled()) entityBoat.move(type, boatMoveEvent.x, boatMoveEvent.y, boatMoveEvent.z);
    }
}
