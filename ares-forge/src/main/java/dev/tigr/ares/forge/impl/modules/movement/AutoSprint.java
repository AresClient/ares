package dev.tigr.ares.forge.impl.modules.movement;

import dev.tigr.ares.core.feature.module.Category;
import dev.tigr.ares.core.feature.module.Module;
import dev.tigr.ares.forge.impl.modules.player.Freecam;

/**
 * @author Tigermouthbear
 */
@Module.Info(name = "AutoSprint", description = "Makes player always sprinting in any direction", category = Category.MOVEMENT)
public class AutoSprint extends Module {
    private static final float SPRINT_SPEED = 0.28061673f;

    @Override
    public void onMotion() {
        if(!Freecam.INSTANCE.getEnabled() && (MC.player.movementInput.moveForward != 0 || MC.player.movementInput.moveStrafe != 0)) {
            MC.player.setSprinting(true);

            if(!MC.player.onGround) return;

            float yaw = MC.player.rotationYaw;
            float forward = 1;

            if(MC.player.moveForward < 0) {
                yaw += 180;
                forward = -0.5f;
            } else if(MC.player.moveForward > 0) forward = 0.5f;

            if(MC.player.moveStrafing > 0) yaw -= 90 * forward;
            if(MC.player.moveStrafing < 0) yaw += 90 * forward;

            yaw = (float) Math.toRadians(yaw);

            MC.player.motionX = -Math.sin(yaw) * SPRINT_SPEED;
            MC.player.motionZ = Math.cos(yaw) * SPRINT_SPEED;
        }
    }
}
