package dev.tigr.ares.forge.impl.modules.render;

import dev.tigr.ares.core.feature.module.Category;
import dev.tigr.ares.core.feature.module.Module;
import dev.tigr.ares.core.setting.Setting;
import dev.tigr.ares.core.setting.settings.EnumSetting;
import dev.tigr.ares.forge.event.events.render.GammaEvent;
import dev.tigr.simpleevents.listener.EventHandler;
import dev.tigr.simpleevents.listener.EventListener;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;

/**
 * @author Tigermouthbear
 */
@Module.Info(name = "FullBright", description = "Lets you see everything with full brightness", category = Category.RENDER)
public class FullBright extends Module {
    private final Setting<VisionMode> mode = register(new EnumSetting<>("Mode", VisionMode.GAMMA));

    @Override
    public void onDisable() {
        if(mode.getValue() == VisionMode.NIGHTVISION && MC.player != null) MC.player.removePotionEffect(Potion.getPotionById(16));
    }

    @Override
    public void onTick() {
        if(mode.getValue() == VisionMode.NIGHTVISION) MC.player.addPotionEffect(new PotionEffect(Potion.getPotionById(16), 69420, 1));
    }

    @EventHandler
    public EventListener<GammaEvent> gammaEvent = new EventListener<>(event -> {
        if(mode.getValue() == VisionMode.GAMMA) event.setGamma(100F);
    });

    enum VisionMode {NIGHTVISION, GAMMA}
}
