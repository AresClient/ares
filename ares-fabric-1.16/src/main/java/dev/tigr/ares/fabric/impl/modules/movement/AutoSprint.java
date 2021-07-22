package dev.tigr.ares.fabric.impl.modules.movement;

import dev.tigr.ares.core.feature.module.Category;
import dev.tigr.ares.core.feature.module.Module;

/**
 * @author Tigermouthbear
 */
@Module.Info(name = "AutoSprint", description = "Makes player always sprinting in any direction", category = Category.MOVEMENT)
public class AutoSprint extends Module {
    private static final float SPRINT_SPEED = 0.28061673f;

    @Override
    public void onMotion() {
        if(MC.player.input.movementForward != 0 || MC.player.input.movementSideways != 0) {
            MC.player.setSprinting(true);

            if(!MC.player.isOnGround()) return;

            float yaw = MC.player.yaw;
            float forward = 1;

            if(MC.player.forwardSpeed < 0) {
                yaw += 180;
                forward = -0.5f;
            } else if(MC.player.forwardSpeed > 0) forward = 0.5f;

            if(MC.player.sidewaysSpeed > 0) yaw -= 90 * forward;
            if(MC.player.sidewaysSpeed < 0) yaw += 90 * forward;

            yaw = (float) Math.toRadians(yaw);

            MC.player.setVelocity(-Math.sin(yaw) * SPRINT_SPEED, MC.player.getVelocity().y, Math.cos(yaw) * SPRINT_SPEED);
        }
    }
}
