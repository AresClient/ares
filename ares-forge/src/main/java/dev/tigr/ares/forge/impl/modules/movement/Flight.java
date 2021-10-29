package dev.tigr.ares.forge.impl.modules.movement;

import dev.tigr.ares.core.feature.module.Category;
import dev.tigr.ares.core.feature.module.Module;
import dev.tigr.ares.core.setting.Setting;
import dev.tigr.ares.core.setting.settings.EnumSetting;
import dev.tigr.ares.core.setting.settings.numerical.FloatSetting;
import dev.tigr.ares.forge.utils.entity.EntityUtils;

/**
 * @author Tigermouthbear
 */
@Module.Info(name = "Flight", description = "Various modes of flying in survival", category = Category.MOVEMENT)
public class Flight extends Module {
    private final Setting<Mode> mode = register(new EnumSetting<>("Mode", Mode.VELOCITY));
    private final Setting<Float> speed = register(new FloatSetting("Speed", 5, 1, 20));

    @Override
    public void onMotion() {
        if(MC.player == null) return;

        switch(mode.getValue()) {
            case VANILLA:
                MC.player.capabilities.setFlySpeed((speed.getValue() / 20) / 10);
                MC.player.capabilities.allowFlying = true;
                break;

            case VELOCITY:
                MC.player.setVelocity(0, 0, 0);
                EntityUtils.moveEntityWithSpeed(MC.player, speed.getValue() / 20, true);
                break;

            case JETPACK:
                if(MC.gameSettings.keyBindJump.isKeyDown()) MC.player.motionY += speed.getValue() / 50d;
                break;
        }
    }

    @Override
    public void onDisable() {
        MC.player.capabilities.allowFlying = false;
    }

    enum Mode {VANILLA, VELOCITY, JETPACK}
}
