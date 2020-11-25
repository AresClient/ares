package dev.tigr.ares.forge.impl.modules.player;

import dev.tigr.ares.core.feature.module.Category;
import dev.tigr.ares.core.feature.module.Module;
import dev.tigr.ares.forge.event.events.player.AntiHitboxEvent;
import dev.tigr.simpleevents.listener.EventHandler;
import dev.tigr.simpleevents.listener.EventListener;

/**
 * @author Tigermouthbear
 */
@Module.Info(name = "AntiHitbox", description = "Ignores hitboxes for placing and breaking blocks", category = Category.PLAYER)
public class AntiHitbox extends Module {
    @EventHandler
    public EventListener<AntiHitboxEvent> antiHitboxEvent = new EventListener<>(event -> {
        if(MC.playerController.getIsHittingBlock()) event.setAllowed(true);
    });
}
