package dev.tigr.ares.forge.impl.modules.movement;

import dev.tigr.ares.core.feature.module.Category;
import dev.tigr.ares.core.feature.module.Module;
import dev.tigr.ares.core.setting.Setting;
import dev.tigr.ares.core.setting.settings.numerical.IntegerSetting;
import dev.tigr.ares.core.util.global.ReflectionHelper;
import net.minecraft.client.Minecraft;

/**
 * @author Tigermouthbear
 */
@Module.Info(name = "Timer", description = "Sets client side tps", category = Category.MOVEMENT)
public class Timer extends Module {
    private final Setting<Integer> tps = register(new IntegerSetting("TPS", 25, 1, 160));

    @Override
    public void onTick() {
        ReflectionHelper.setPrivateValue(net.minecraft.util.Timer.class, ReflectionHelper.getPrivateValue(Minecraft.class, MC, "timer", "field_71428_T"), 1000.0F / tps.getValue(), "tickLength", "field_194149_e");
    }

    @Override
    public void onDisable() {
        ReflectionHelper.setPrivateValue(net.minecraft.util.Timer.class, ReflectionHelper.getPrivateValue(Minecraft.class, MC, "timer", "field_71428_T"), 1000.0F / 20.0F, "tickLength", "field_194149_e");
    }
}
