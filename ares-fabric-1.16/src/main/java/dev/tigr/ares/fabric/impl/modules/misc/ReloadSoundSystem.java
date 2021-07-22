package dev.tigr.ares.fabric.impl.modules.misc;

import dev.tigr.ares.core.feature.module.Category;
import dev.tigr.ares.core.feature.module.Module;
import dev.tigr.ares.fabric.mixin.accessors.SoundManagerAccessor;

/**
 * @author Tigermouthbear
 */
@Module.Info(name = "ReloadSoundSys", description = "Reloads Minecraft's sound system", category = Category.MISC)
public class ReloadSoundSystem extends Module {
    @Override
    public void onEnable() {
        ((SoundManagerAccessor) MC.getSoundManager()).getSoundSystem().reloadSounds();
        setEnabled(false);
    }
}
