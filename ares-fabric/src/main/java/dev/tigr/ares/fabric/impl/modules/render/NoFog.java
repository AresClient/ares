package dev.tigr.ares.fabric.impl.modules.render;

import dev.tigr.ares.core.feature.module.Category;
import dev.tigr.ares.core.feature.module.Module;
import dev.tigr.ares.fabric.event.render.SetupFogEvent;
import dev.tigr.simpleevents.listener.EventHandler;
import dev.tigr.simpleevents.listener.EventListener;

/**
 * @author Tigermouthbear
 */
@Module.Info(name = "NoFog", description = "Prevents rendering of fog", category = Category.RENDER)
public class NoFog extends Module {
    @EventHandler
    public EventListener<SetupFogEvent> setupFogEvent = new EventListener<>(event -> event.setCancelled(true));
}
