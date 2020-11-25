package dev.tigr.ares.forge.impl.modules.movement;

import dev.tigr.ares.core.feature.module.Category;
import dev.tigr.ares.core.feature.module.Module;

/**
 * @author Tigermouthbear
 */
@Module.Info(name = "AutoSprint", description = "Makes player always sprinting in any direction", category = Category.MOVEMENT)
public class AutoSprint extends Module {
    @Override
    public void onMotion() {
        MC.player.setSprinting(true);
    }
}
