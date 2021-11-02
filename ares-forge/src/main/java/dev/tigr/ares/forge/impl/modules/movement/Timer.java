package dev.tigr.ares.forge.impl.modules.movement;

import dev.tigr.ares.core.feature.module.Category;
import dev.tigr.ares.core.feature.module.Module;
import dev.tigr.ares.core.setting.Setting;
import dev.tigr.ares.core.setting.settings.EnumSetting;
import dev.tigr.ares.core.setting.settings.numerical.FloatSetting;
import dev.tigr.ares.core.util.global.ReflectionHelper;
import net.minecraft.client.Minecraft;

/**
 * @author Tigermouthbear
 */
@Module.Info(name = "Timer", description = "Sets client side tps", category = Category.MOVEMENT)
public class Timer extends Module {
    private final Setting<Float> tps = register(new FloatSetting("Speed", 1.088f, 0.1f, 20f));
    private final Setting<when> mode = register(new EnumSetting<when>("Mode", when.ALWAYS));

    public static Timer INSTANCE;

    public Timer() {
        INSTANCE = this;
    }

    @Override
    public void onTick() {
        if (!(MC.player.moveForward == 0) && mode.getValue() == when.MOTION
        || mode.getValue() == when.ALWAYS){
            ReflectionHelper.setPrivateValue(net.minecraft.util.Timer.class, ReflectionHelper.getPrivateValue(Minecraft.class, MC, "timer", "field_71428_T"), (1000.0F / tps.getValue()) / 20, "tickLength", "field_194149_e");
        }
    }

    @Override
    public void onDisable() {
        ReflectionHelper.setPrivateValue(net.minecraft.util.Timer.class, ReflectionHelper.getPrivateValue(Minecraft.class, MC, "timer", "field_71428_T"), 1000.0F / 20.0F, "tickLength", "field_194149_e");
    }

    enum when {ALWAYS, MOTION}

}
