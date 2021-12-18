package dev.tigr.ares.core.util.entity;

import dev.tigr.ares.Wrapper;
import dev.tigr.ares.core.util.math.MathUtils;
import dev.tigr.ares.core.util.math.doubles.V2D;

public class SelfUtils implements Wrapper {
    public static V2D getMovement(final double speed) {
        float
                forward = SELF.getInputMovementForward(),
                sideways = SELF.getInputMovementSideways(),
                yaw = SELF.getPrevYaw() + (SELF.getYaw() - SELF.getPrevYaw()) * UTILS.getRenderPartialTicks();
        return MathUtils.getMovement(speed, forward, sideways, yaw);
    }
}
