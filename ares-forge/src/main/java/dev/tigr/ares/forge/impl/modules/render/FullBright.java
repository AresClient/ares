package dev.tigr.ares.forge.impl.modules.render;

import dev.tigr.ares.core.feature.module.Category;
import dev.tigr.ares.core.feature.module.Module;
import dev.tigr.ares.core.setting.Setting;
import dev.tigr.ares.core.setting.settings.EnumSetting;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;

/**
 * @author Tigermouthbear
 */
@Module.Info(name = "FullBright", description = "Lets you see everything with full brightness", category = Category.RENDER)
public class FullBright extends Module {
    private final Setting<visionMode> mode = register(new EnumSetting<>("Mode", visionMode.GAMMA));
    private float prevGamma = -1;

    @Override
    public void onEnable() {
        prevGamma = MC.gameSettings.gammaSetting;
    }

    @Override
    public void onDisable() {
        if(prevGamma == -1)
            return;

        MC.gameSettings.gammaSetting = prevGamma;
        prevGamma = -1;
        if(MC.player != null) MC.player.removePotionEffect(Potion.getPotionById(16));
    }

    @Override
    public void onTick() {
        switch(mode.getValue()) {
            case NIGHTVISION:
                MC.player.addPotionEffect(new PotionEffect(Potion.getPotionById(16), 69420, 1));
                break;

            default:
            case GAMMA:
                if(MC.gameSettings.gammaSetting <= 100F)
                    MC.gameSettings.gammaSetting++;
                break;
        }
    }

    enum visionMode {NIGHTVISION, GAMMA}
}
