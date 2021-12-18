package dev.tigr.ares.core.util.entity;

import dev.tigr.ares.Wrapper;
import dev.tigr.ares.core.util.math.doubles.V3D;

public class EntityUtils implements Wrapper {
    public static void moveEntityWithSpeed(int entity, double speed) {
        moveEntityWithSpeed(entity, speed, false, 0);
    }

    public static void moveEntityWithSpeed(int entity, double speedHorz, boolean shouldMoveY, double speedVert) {
        V3D motion = new V3D(SelfUtils.getMovement(speedHorz));

        if(shouldMoveY) {
            if(SELF.getInputJumping()) {
                if(!SELF.getInputSneaking())
                    motion.setY(speedVert);
            }

            else if(SELF.getInputSneaking())
                motion.setY(-speedVert);
        }

        ENTITY.setVelocity(entity, motion);
    }
}
