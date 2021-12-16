package dev.tigr.ares.core.feature.module.modules.player;

import dev.tigr.ares.core.feature.module.Category;
import dev.tigr.ares.core.feature.module.Module;
import dev.tigr.ares.core.setting.Setting;
import dev.tigr.ares.core.setting.settings.BooleanSetting;
import dev.tigr.ares.core.setting.settings.numerical.IntegerSetting;

/**
 * @author Makrennel 10/11/21
 * 2021/12/16 - Functionality moved to RotationTracker, this now just exposes settings
 */
@Module.Info(name = "Rotations", description = "Exposes general settings for managing rotations", category = Category.PLAYER, enabled = true, visible = false)
public class Rotations extends Module {
    public static Rotations ROTATION_SETTINGS;

    public Rotations() {
        ROTATION_SETTINGS = this;
    }

    public final Setting<Integer> resetDelay = register(new IntegerSetting("Reset Delay", 20, 0, 40));
    public final Setting<Boolean> renderBodyYaw = register(new BooleanSetting("Render Body Yaw", true));

    @Override
    public void onDisable() {
        setEnabled(true);
    }
}
