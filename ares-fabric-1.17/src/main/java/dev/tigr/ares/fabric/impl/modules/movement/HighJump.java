package dev.tigr.ares.fabric.impl.modules.movement;

import dev.tigr.ares.core.feature.module.Category;
import dev.tigr.ares.core.feature.module.Module;
import dev.tigr.ares.core.setting.Setting;
import dev.tigr.ares.core.setting.settings.numerical.FloatSetting;

/**
 * @author Tigermouthbear 8/30/20
 */
@Module.Info(name = "HighJump", description = "Jump higher!", category = Category.MOVEMENT)
public class HighJump extends Module {
    private static HighJump INSTANCE;

    private Setting<Float> multiplier = register(new FloatSetting("Multiplier", 1.5f, 0, 4));

    public HighJump() {
        INSTANCE = this;
    }

    public static float getMultiplier() {
        return INSTANCE.getEnabled() ? INSTANCE.multiplier.getValue() : 1f;
    }
}
