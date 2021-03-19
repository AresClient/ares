package dev.tigr.ares.fabric.impl.modules.misc;

import dev.tigr.ares.core.feature.module.Category;
import dev.tigr.ares.core.feature.module.Module;
import dev.tigr.ares.core.util.global.ReflectionHelper;
import net.minecraft.client.sound.SoundManager;
import net.minecraft.client.sound.SoundSystem;

/**
 * @author Tigermouthbear
 */
@Module.Info(name = "ReloadSoundSys", description = "Reloads Minecraft's sound system", category = Category.MISC)
public class ReloadSoundSystem extends Module {
    @Override
    public void onEnable() {
        SoundSystem soundSystem = ReflectionHelper.getPrivateValue(SoundManager.class, MC.getSoundManager(), "soundSystem", "field_5590");
        soundSystem.reloadSounds();
        setEnabled(false);
    }
}
