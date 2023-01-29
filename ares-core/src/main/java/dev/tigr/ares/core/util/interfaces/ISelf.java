package dev.tigr.ares.core.util.interfaces;

import dev.tigr.ares.core.util.math.doubles.V2D;
import dev.tigr.ares.core.util.math.doubles.V3D;

public interface ISelf {
    boolean isOnGround();

    boolean isInLava();

    boolean isInWater();

    float getYaw();

    float getPrevYaw();

    float getPitch();

    void setSprinting(boolean sprinting);

    float getInputMovementForward();

    float getInputMovementSideways();

    boolean getInputJumping();

    boolean getInputSneaking();

    boolean isPotionActive(int potionID);

    int getPotionAmplifier(int potionID);

    V3D getVelocity();

    default void addVelocity(V3D velocity) {
        addVelocity(velocity.x, velocity.y, velocity.z);
    }

    default void addVelocity(V2D xzVelocity) {
        addVelocity(xzVelocity.a, 0, xzVelocity.b);
    }

    void addVelocity(double x, double y, double z);

    default void setVelocity(V3D velocity) {
        setVelocity(velocity.x, velocity.y, velocity.z);
    }

    void setVelocity(double x, double y, double z);

    void copyFrom(int entity);

    int getId();

    boolean isRiding();

    void startRiding(int entity);

    void stopRiding();

    void setPose(String pose);

    void setNoClip(boolean noClip);

    int getRidingEntity();

    boolean collidedHorizontally();

    V3D getPositionDelta();
}
