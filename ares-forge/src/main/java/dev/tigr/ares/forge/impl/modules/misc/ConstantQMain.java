package dev.tigr.ares.forge.impl.modules.misc;

import dev.tigr.ares.core.feature.module.Category;
import dev.tigr.ares.core.feature.module.Module;
import dev.tigr.ares.core.setting.Setting;
import dev.tigr.ares.core.setting.settings.BooleanSetting;

/**
 * @author Tigermouthbear
 */
@Module.Info(name = "ConstantQMain", description = "Types /queue main in chat every 30s", category = Category.MISC)
public class ConstantQMain extends Module {
    private static long lastSentCmd = 0;
    private final Setting<Boolean> onlyEnd = register(new BooleanSetting("Only in end", true));

    @Override
    public void onTick() {
        if(System.currentTimeMillis() >= (lastSentCmd + 30 * 1000)) {
            if(MC.player == null) return;

            if(!onlyEnd.getValue() || (MC.player.dimension != -1 && MC.player.dimension != 0)) {
                lastSentCmd = System.currentTimeMillis();
                MC.player.sendChatMessage("/queue main");
                UTILS.printMessage("/queue main");
            }
        }
    }

    @Override
    public void onDisable() {
        lastSentCmd = 0;
    }
}
