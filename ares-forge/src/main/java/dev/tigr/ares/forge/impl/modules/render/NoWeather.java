package dev.tigr.ares.forge.impl.modules.render;

import dev.tigr.ares.core.feature.module.Category;
import dev.tigr.ares.core.feature.module.Module;
import dev.tigr.simpleevents.listener.EventHandler;
import dev.tigr.simpleevents.listener.EventListener;
import net.minecraftforge.event.entity.living.LivingEvent;

/**
 * @author Tigermouthbear
 */
@Module.Info(name = "NoWeather", description = "Stops rain from falling", category = Category.RENDER)
public class NoWeather extends Module {
    @EventHandler
    public EventListener<LivingEvent.LivingUpdateEvent> livingUpdateEvent = new EventListener<>(event -> {
        if(MC.world.isRaining()) MC.world.setRainStrength(0);
    });
}
