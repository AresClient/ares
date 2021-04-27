package dev.tigr.ares.forge.impl.modules.movement;

import dev.tigr.ares.core.feature.module.Category;
import dev.tigr.ares.core.feature.module.Module;
import dev.tigr.ares.core.setting.Setting;
import dev.tigr.ares.core.setting.settings.BooleanSetting;
import dev.tigr.ares.core.setting.settings.numerical.DoubleSetting;
import dev.tigr.ares.core.setting.settings.numerical.FloatSetting;

/**
 * @author Tigermouthbear
 */
@Module.Info(name = "Strafe", description = "Increase speed and control in air", category = Category.MOVEMENT)
public class Strafe extends Module {
    private final Setting<Boolean> lowHop = register(new BooleanSetting("Low Hop", false));
    private final Setting<Double> height = register(new DoubleSetting("Height", 0.3, 0.3, 0.5)).setVisibility(lowHop::getValue);
    private final Setting<Boolean> speedBool = register(new BooleanSetting("Modify Speed", true));
    private final Setting<Float> speedVal = register(new FloatSetting("Speed", 0.32f, 0.2f, 0.6f)).setVisibility(speedBool::getValue);
    private final Setting<Boolean> sprintBool = register(new BooleanSetting("Auto Sprint", true));

    @Override
    public void onMotion() {
        if(MC.player.movementInput.moveForward != 0 || MC.player.movementInput.moveStrafe != 0) {
            if(sprintBool.getValue()) {
                MC.player.setSprinting(true);
            }

            if(MC.player.onGround && lowHop.getValue()) MC.player.motionY += height.getValue();

            if(MC.player.onGround) return;

            float speed;
            if(!speedBool.getValue())
                speed = (float) Math.sqrt(MC.player.motionX * MC.player.motionX + MC.player.motionZ * MC.player.motionZ);
            else speed = speedVal.getValue();

            float yaw = MC.player.rotationYaw;
            float forward = 1;

            if(MC.player.moveForward < 0) {
                yaw += 180;
                forward = -0.5f;
            } else if(MC.player.moveForward > 0) forward = 0.5f;

            if(MC.player.moveStrafing > 0) yaw -= 90 * forward;
            if(MC.player.moveStrafing < 0) yaw += 90 * forward;

            yaw = (float) Math.toRadians(yaw);

            MC.player.motionX = -Math.sin(yaw) * speed;
            MC.player.motionZ = Math.cos(yaw) * speed;
        }
    }
}
