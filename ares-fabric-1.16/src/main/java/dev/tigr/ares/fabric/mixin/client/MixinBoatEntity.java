package dev.tigr.ares.fabric.mixin.client;

import dev.tigr.ares.core.Ares;
import dev.tigr.ares.fabric.event.movement.BoatMoveEvent;
import net.minecraft.entity.MovementType;
import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * @author Tigermouthbear 12/16/20
 */
@Mixin(BoatEntity.class)
public class MixinBoatEntity {
    @Redirect(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/vehicle/BoatEntity;move(Lnet/minecraft/entity/MovementType;Lnet/minecraft/util/math/Vec3d;)V"))
    public void move(BoatEntity boatEntity, MovementType type, Vec3d movement) {
        BoatMoveEvent boatMoveEvent = Ares.EVENT_MANAGER.post(new BoatMoveEvent(boatEntity, movement.x, movement.y, movement.z));
        if(!boatMoveEvent.isCancelled()) boatEntity.move(type, new Vec3d(boatMoveEvent.x, boatMoveEvent.y, boatMoveEvent.z));
    }
}
