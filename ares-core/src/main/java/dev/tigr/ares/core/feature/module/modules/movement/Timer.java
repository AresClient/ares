package dev.tigr.ares.core.feature.module.modules.movement;

import dev.tigr.ares.core.feature.module.Category;
import dev.tigr.ares.core.feature.module.Module;
import dev.tigr.ares.core.setting.Setting;
import dev.tigr.ares.core.setting.settings.EnumSetting;
import dev.tigr.ares.core.setting.settings.numerical.FloatSetting;

/**
 * @author Tigermouthbear
 * moved to core - Makrennel - 2021/12/17
 */
@Module.Info(name = "Timer", description = "Sets client side tps", category = Category.MOVEMENT)
public class Timer extends Module {
    public static Timer INSTANCE;

    public Timer() {
        INSTANCE = this;
    }

    private final Setting<Float> tps = register(new FloatSetting("Speed", 1.0f, 0.1f, 20f));
    private final Setting<When> mode = register(new EnumSetting<>("Mode", When.ALWAYS));

    enum When {ALWAYS, MOTION}

    @Override
    public void onTick() {
        if(SELF.getInputMovementForward() == 0 && SELF.getInputMovementSideways() == 0 && mode.getValue() != When.ALWAYS) {
            UTILS.setTickLength(50);
            return;
        }
        UTILS.setTpsMultiplier(tps.getValue());
    }

    @Override
    public void onDisable() {
        UTILS.setTickLength(50);
    }
}
