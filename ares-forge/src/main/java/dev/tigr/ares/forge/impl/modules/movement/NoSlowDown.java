package dev.tigr.ares.forge.impl.modules.movement;

import dev.tigr.ares.core.feature.module.Category;
import dev.tigr.ares.core.feature.module.Module;
import dev.tigr.ares.forge.event.events.movement.SlowDownEvent;
import dev.tigr.simpleevents.listener.EventHandler;
import dev.tigr.simpleevents.listener.EventListener;
import net.minecraftforge.client.event.InputUpdateEvent;

/**
 * @author Tigermouthbear 7/14/20
 */
@Module.Info(name = "NoSlowDown", description = "Prevents player from slowing down while eating and other events", category = Category.MOVEMENT)
public class NoSlowDown extends Module {
    @EventHandler
    public EventListener<InputUpdateEvent> inputUpdateEvent = new EventListener<>(event -> {
        if(!MC.player.isRiding() && MC.player.isHandActive()) {
            event.getMovementInput().moveStrafe *= 5;
            event.getMovementInput().moveForward *= 5;
        }
    });

    @EventHandler
    public EventListener<SlowDownEvent> slowDownEvent = new EventListener<>(event -> event.setCancelled(true));
}
