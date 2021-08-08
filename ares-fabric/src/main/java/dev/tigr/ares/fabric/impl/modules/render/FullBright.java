package dev.tigr.ares.fabric.impl.modules.render;

import dev.tigr.ares.core.feature.module.Category;
import dev.tigr.ares.core.feature.module.Module;
import dev.tigr.ares.core.setting.Setting;
import dev.tigr.ares.core.setting.settings.EnumSetting;
import dev.tigr.ares.fabric.event.render.GammaEvent;
import dev.tigr.simpleevents.listener.EventHandler;
import dev.tigr.simpleevents.listener.EventListener;
import net.minecraft.potion.Potions;

/**
 * @author Tigermouthbear
 */
@Module.Info(name = "FullBright", description = "Lets you see everything with full brightness", category = Category.RENDER)
public class FullBright extends Module {
    private final Setting<VisionMode> mode = register(new EnumSetting<>("Mode", VisionMode.GAMMA));

    @Override
    public void onDisable() {
        if(mode.getValue() == VisionMode.NIGHTVISION && MC.player != null) Potions.NIGHT_VISION.getEffects().forEach(statusEffectInstance -> MC.player.removeStatusEffect(statusEffectInstance.getEffectType()));
    }

    @Override
    public void onTick() {
        if(mode.getValue() == VisionMode.NIGHTVISION) Potions.NIGHT_VISION.getEffects().forEach(statusEffectInstance -> MC.player.setStatusEffect(statusEffectInstance, null));
    }

    @EventHandler
    public EventListener<GammaEvent> gammaEvent = new EventListener<>(event -> {
        if(mode.getValue() == VisionMode.GAMMA) event.setGamma(100F);
    });

    enum VisionMode {NIGHTVISION, GAMMA}
}
