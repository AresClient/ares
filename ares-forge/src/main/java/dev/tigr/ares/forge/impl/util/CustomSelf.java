package dev.tigr.ares.forge.impl.util;

import dev.tigr.ares.core.util.interfaces.ISelf;
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
    public float getPitch() {
        return MC.player.rotationPitch;
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
    public boolean getInputJumping() {
        return MC.player.movementInput.jump;
    }

    @Override
    public boolean getInputSneaking() {
        return MC.player.movementInput.sneak;
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
    public void addVelocity(double x, double y, double z) {
        MC.player.addVelocity(x, y, z);
    }

    @Override
    public void setVelocity(double x, double y, double z) {
        MC.player.setVelocity(x, y, z);
    }

    @Override
    public void copyFrom(int entity) {
        MC.player.copyLocationAndAnglesFrom(MC.world.getEntityByID(entity));
    }

    @Override
    public int getId() {
        return MC.player.getEntityId();
    }

    @Override
    public boolean isRiding() {
        return MC.player.isRiding();
    }

    @Override
    public void startRiding(int entity) {
        MC.player.startRiding(MC.world.getEntityByID(entity));
    }

    @Override
    public void stopRiding() {
        MC.player.dismountRidingEntity();
    }

    @Override
    public void setPose(String pose) {
        // Pose? What's that?
    }

    @Override
    public void setNoClip(boolean noClip) {
        MC.player.noClip = noClip;
    }

    @Override
    public int getRidingEntity() {
        return MC.player.getRidingEntity().getEntityId();
    }

    @Override
    public boolean collidedHorizontally() {
        return MC.player.collidedHorizontally;
    }

    @Override
    public V3D getPositionDelta() {
        return new V3D(MC.player.posX - MC.player.prevPosX, MC.player.posY - MC.player.prevPosY, MC.player.posZ - MC.player.prevPosZ);
    }

}
