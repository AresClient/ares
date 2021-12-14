package dev.tigr.ares.fabric.impl.util;

import dev.tigr.ares.core.util.interfaces.ISelf;
import dev.tigr.ares.core.util.math.doubles.V2D;
import dev.tigr.ares.core.util.math.doubles.V3D;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.util.math.Vec3d;

@SuppressWarnings("ConstantConditions")
public class CustomSelf implements ISelf {
    MinecraftClient MC = MinecraftClient.getInstance();

    @Override
    public boolean isOnGround() {
        return MC.player.isOnGround();
    }

    @Override
    public boolean isInLava() {
        return MC.player.isInLava();
    }

    @Override
    public boolean isInWater() {
        return MC.player.isSubmergedInWater();
    }

    @Override
    public float getYaw() {
        return MC.player.yaw;
    }

    @Override
    public float getPrevYaw() {
        return MC.player.prevYaw;
    }

    @Override
    public void setSprinting(boolean sprinting) {
        MC.player.setSprinting(sprinting);
    }

    @Override
    public float getInputMovementForward() {
        return MC.player.forwardSpeed;
    }

    @Override
    public float getInputMovementSideways() {
        return MC.player.sidewaysSpeed;
    }

    @Override
    public boolean isPotionActive(int potionID) {
        return MC.player.getActiveStatusEffects().containsValue(MC.player.getStatusEffect(StatusEffect.byRawId(1)));
    }

    @Override
    public int getPotionAmplifier(int potionID) {
        return MC.player.getStatusEffect(StatusEffect.byRawId(1)).getAmplifier();
    }

    @Override
    public V3D getVelocity() {
        Vec3d vel = MC.player.getVelocity();
        return new V3D(vel.x, vel.y, vel.z);
    }

    @Override
    public void addVelocity(V3D velocity) {
        MC.player.addVelocity(velocity.getX(), velocity.getY(), velocity.getZ());
    }

    @Override
    public void addVelocity(V2D xzVelocity) {
        MC.player.addVelocity(xzVelocity.getA(), 0, xzVelocity.getB());
    }

    @Override
    public void addVelocity(double x, double y, double z) {
        MC.player.addVelocity(x, y, z);
    }

    @Override
    public void setVelocity(V3D velocity) {
        MC.player.setVelocity(velocity.getX(), velocity.getY(), velocity.getZ());
    }
}
