package dev.tigr.ares.core.util.interfaces;

import dev.tigr.ares.core.util.math.doubles.V2D;
import dev.tigr.ares.core.util.math.doubles.V3D;

public interface ISelf {
    boolean isOnGround();

    boolean isInLava();

    boolean isInWater();

    float getYaw();

    float getPrevYaw();

    void setSprinting(boolean sprinting);

    float getInputMovementForward();

    float getInputMovementSideways();

    boolean isPotionActive(int potionID);

    int getPotionAmplifier(int potionID);

    V3D getVelocity();

    void addVelocity(V3D velocity);

    void addVelocity(V2D xzVelocity);

    void addVelocity(double x, double y, double z);

    void setVelocity(V3D velocity);
}
