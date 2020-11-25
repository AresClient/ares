package dev.tigr.ares.forge.impl.modules.misc;

import dev.tigr.ares.core.feature.module.Category;
import dev.tigr.ares.core.feature.module.Module;
import dev.tigr.ares.core.util.global.ReflectionHelper;
import net.minecraft.client.audio.SoundHandler;
import net.minecraft.client.audio.SoundManager;

/**
 * @author Tigermouthbear
 */
@Module.Info(name = "ReloadSoundSys", description = "Reloads Minecraft's sound system", category = Category.MISC)
public class ReloadSoundSystem extends Module {
    @Override
    public void onEnable() {
        SoundManager sndManager = ReflectionHelper.getPrivateValue(SoundHandler.class, MC.getSoundHandler(), "sndManager", "field_147694_f");
        sndManager.reloadSoundSystem();
        setEnabled(false);
    }
}
