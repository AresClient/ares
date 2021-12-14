package dev.tigr.ares.forge.impl.util;

import dev.tigr.ares.core.util.interfaces.ISelf;
import dev.tigr.ares.core.util.math.doubles.V2D;
import dev.tigr.ares.core.util.math.doubles.V3D;
import net.minecraft.client.Minecraft;
import net.minecraft.potion.Potion;

@SuppressWarnings("ConstantConditions")
public class CustomSelf implements ISelf {
    Minecraft MC = Minecraft.getMinecraft();

    @Override
    public boolean isOnGround() {
        return MC.player.onGround;
    }

    @Override
    public boolean isInLava() {
        return MC.player.isInLava();
    }

    @Override
    public boolean isInWater() {
        return MC.player.isInWater();
    }

    @Override
    public float getYaw() {
        return MC.player.rotationYaw;
    }

    @Override
    public float getPrevYaw() {
        return MC.player.prevRotationYaw;
    }

    @Override
    public void setSprinting(boolean sprinting) {
        MC.player.setSprinting(sprinting);
    }

    @Override
    public float getInputMovementForward() {
        return MC.player.moveForward;
    }

    @Override
    public float getInputMovementSideways() {
        return MC.player.moveStrafing;
    }

    @Override
    public boolean isPotionActive(int potionID) {
        return MC.player.isPotionActive(Potion.getPotionById(1));
    }

    @Override
    public int getPotionAmplifier(int potionID) {
        return MC.player.getActivePotionEffect(Potion.getPotionById(1)).getAmplifier();
    }

    @Override
    public V3D getVelocity() {
        return new V3D(MC.player.motionX, MC.player.motionY, MC.player.motionZ);
    }

    @Override
    public void addVelocity(V3D velocity) {
        MC.player.addVelocity(velocity.getX(), velocity.getY(), velocity.getZ());
    }

    @Override
    public void addVelocity(V2D xzVelocity) {
        MC.player.motionX += xzVelocity.getA();
        MC.player.motionZ += xzVelocity.getB();
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
