package dev.tigr.ares.forge.impl.modules.render;

import dev.tigr.ares.core.feature.module.Category;
import dev.tigr.ares.core.feature.module.Module;
import dev.tigr.ares.forge.event.events.render.HurtCamEvent;
import dev.tigr.simpleevents.listener.EventHandler;
import dev.tigr.simpleevents.listener.EventListener;

/**
 * @author Tigermouthbear
 */
@Module.Info(name = "NoHurtShake", description = "Prevents camera from shaking on damage", category = Category.RENDER)
public class NoHurtShake extends Module {
    @EventHandler
    public EventListener<HurtCamEvent> hurtCamEvent = new EventListener<>(event -> event.setCancelled(true));
}
