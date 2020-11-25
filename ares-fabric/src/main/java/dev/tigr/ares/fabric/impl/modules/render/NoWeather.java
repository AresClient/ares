package dev.tigr.ares.fabric.impl.modules.render;

import dev.tigr.ares.core.feature.module.Category;
import dev.tigr.ares.core.feature.module.Module;


/**
 * @author Tigermouthbear
 */
@Module.Info(name = "NoWeather", description = "Stops rain from falling", category = Category.RENDER)
public class NoWeather extends Module {
    @Override
    public void onTick() {
        if(MC.world.isRaining()) MC.world.setRainGradient(0);
    }
}
