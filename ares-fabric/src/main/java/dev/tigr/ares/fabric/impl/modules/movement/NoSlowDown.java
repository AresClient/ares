package dev.tigr.ares.fabric.impl.modules.movement;

import dev.tigr.ares.core.feature.module.Category;
import dev.tigr.ares.core.feature.module.Module;
import dev.tigr.ares.fabric.event.movement.SlowDownEvent;
import dev.tigr.simpleevents.listener.EventHandler;
import dev.tigr.simpleevents.listener.EventListener;

/**
 * @author Tigermouthbear 10/3/20
 */
@Module.Info(name = "NoSlowDown", description = "Prevents player from slowing down while eating and other events", category = Category.MOVEMENT)
public class NoSlowDown extends Module {
    @EventHandler
    public EventListener<SlowDownEvent> slowDownEvent = new EventListener<>(event -> event.setCancelled(true));
}
