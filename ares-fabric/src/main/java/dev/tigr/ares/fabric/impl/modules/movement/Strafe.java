package dev.tigr.ares.fabric.impl.modules.movement;

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

    @Override
    public void onMotion() {
        if(MC.player.input.movementForward != 0 || MC.player.input.movementSideways != 0) {
            MC.player.setSprinting(true);

            if(MC.player.isOnGround() && lowHop.getValue()) MC.player.addVelocity(0, height.getValue(), 0);

            if(MC.player.isOnGround()) return;

            float speed;
            if(!speedBool.getValue())
                speed = (float) Math.sqrt(MC.player.getVelocity().x * MC.player.getVelocity().x + MC.player.getVelocity().z * MC.player.getVelocity().z);
            else speed = speedVal.getValue();

            float yaw = MC.player.yaw;
            float forward = 1;

            if(MC.player.forwardSpeed < 0) {
                yaw += 180;
                forward = -0.5f;
            } else if(MC.player.forwardSpeed > 0) forward = 0.5f;

            if(MC.player.sidewaysSpeed > 0) yaw -= 90 * forward;
            if(MC.player.sidewaysSpeed < 0) yaw += 90 * forward;

            yaw = (float) Math.toRadians(yaw);

            MC.player.setVelocity(-Math.sin(yaw) * speed, MC.player.getVelocity().y, Math.cos(yaw) * speed);
        }
    }
}
