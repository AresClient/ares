package dev.tigr.ares.fabric.impl.modules.movement;

import dev.tigr.ares.core.feature.module.Category;
import dev.tigr.ares.core.feature.module.Module;
import dev.tigr.ares.core.setting.Setting;
import dev.tigr.ares.core.setting.settings.EnumSetting;
import dev.tigr.ares.core.setting.settings.numerical.FloatSetting;
import dev.tigr.ares.fabric.utils.entity.EntityUtils;

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
                MC.player.getAbilities().setFlySpeed((speed.getValue() / 20) / 10);
                MC.player.getAbilities().allowFlying = true;
                break;

            case VELOCITY:
                EntityUtils.moveEntityWithSpeed(MC.player, speed.getValue() / 20, true);
                break;

            case JETPACK:
                if(MC.options.keyJump.isPressed()) MC.player.setVelocity(MC.player.getVelocity().x, MC.player.getVelocity().y + speed.getValue() / 50d, MC.player.getVelocity().z);
                break;
        }
    }

    @Override
    public void onDisable() {
        MC.player.getAbilities().allowFlying = false;
    }

    enum Mode {VANILLA, VELOCITY, JETPACK}
}
