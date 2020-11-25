package dev.tigr.ares.fabric.impl.modules.render;

import dev.tigr.ares.core.feature.module.Category;
import dev.tigr.ares.core.feature.module.Module;
import dev.tigr.ares.core.setting.Setting;
import dev.tigr.ares.core.setting.settings.EnumSetting;
import net.minecraft.potion.Potions;

/**
 * @author Tigermouthbear
 */
@Module.Info(name = "FullBright", description = "Lets you see everything with full brightness", category = Category.RENDER)
public class FullBright extends Module {
    private final Setting<visionMode> mode = register(new EnumSetting<>("Mode", visionMode.GAMMA));
    private double prevGamma = -1;

    @Override
    public void onEnable() {
        prevGamma = MC.options.gamma;
    }

    @Override
    public void onDisable() {
        if(prevGamma == -1)
            return;

        MC.options.gamma = prevGamma;
        prevGamma = -1;
        if(MC.player != null) Potions.NIGHT_VISION.getEffects().forEach(statusEffectInstance -> MC.player.removeStatusEffect(statusEffectInstance.getEffectType()));
    }

    @Override
    public void onTick() {
        switch(mode.getValue()) {
            case NIGHTVISION:
                Potions.NIGHT_VISION.getEffects().forEach(statusEffectInstance -> MC.player.applyStatusEffect(statusEffectInstance));
                break;

            default:
            case GAMMA:
                if(MC.options.gamma <= 100d)
                    MC.options.gamma++;
                break;
        }
    }

    enum visionMode {NIGHTVISION, GAMMA}
}
