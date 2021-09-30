package dev.tigr.ares.fabric.impl.modules.movement;

import dev.tigr.ares.core.feature.module.Category;
import dev.tigr.ares.core.feature.module.Module;
import dev.tigr.ares.core.setting.Setting;
import dev.tigr.ares.core.setting.settings.numerical.IntegerSetting;
import dev.tigr.ares.fabric.mixin.accessors.MinecraftClientAccessor;
import dev.tigr.ares.fabric.mixin.accessors.RenderTickCounterAccessor;

/**
 * @author Tigermouthbear
 */
@Module.Info(name = "Timer", description = "Sets client side tps", category = Category.MOVEMENT)
public class Timer extends Module {
    private final Setting<Integer> tps = register(new IntegerSetting("TPS", 25, 1, 160));

    public static Timer INSTANCE;

    public Timer() {
        INSTANCE = this;
    }

    @Override
    public void onTick() {
        ((RenderTickCounterAccessor) ((MinecraftClientAccessor) MC).getRenderTickCounter()).setTickTime(1000.0F / tps.getValue());
    }

    @Override
    public void onDisable() {
        ((RenderTickCounterAccessor) ((MinecraftClientAccessor) MC).getRenderTickCounter()).setTickTime(1000.0F / 20.0F);
    }
}
