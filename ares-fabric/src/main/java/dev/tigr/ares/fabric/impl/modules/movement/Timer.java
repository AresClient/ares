package dev.tigr.ares.fabric.impl.modules.movement;

import dev.tigr.ares.core.feature.module.Category;
import dev.tigr.ares.core.feature.module.Module;
import dev.tigr.ares.core.setting.Setting;
import dev.tigr.ares.core.setting.settings.numerical.IntegerSetting;
import dev.tigr.ares.core.util.global.ReflectionHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.RenderTickCounter;

/**
 * @author Tigermouthbear
 */
@Module.Info(name = "Timer", description = "Sets client side tps", category = Category.MOVEMENT)
public class Timer extends Module {
    private final Setting<Integer> tps = register(new IntegerSetting("TPS", 25, 1, 160));

    @Override
    public void onTick() {
        ReflectionHelper.setPrivateValue(RenderTickCounter.class, ReflectionHelper.getPrivateValue(MinecraftClient.class, MC, "renderTickCounter", "field_1728"), 1000.0F / tps.getValue(), "tickTime", "field_1968");
    }

    @Override
    public void onDisable() {
        ReflectionHelper.setPrivateValue(RenderTickCounter.class, ReflectionHelper.getPrivateValue(MinecraftClient.class, MC, "renderTickCounter", "field_1728"), 1000.0F / 20.0F, "tickTime", "field_1968");
    }
}
