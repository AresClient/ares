package dev.tigr.ares.fabric.impl.modules.player;

import dev.tigr.ares.core.feature.module.Category;
import dev.tigr.ares.core.feature.module.Module;
import dev.tigr.ares.fabric.event.player.AntiHitboxEvent;
import dev.tigr.simpleevents.event.Result;
import dev.tigr.simpleevents.listener.EventHandler;
import dev.tigr.simpleevents.listener.EventListener;

/**
 * @author Tigermouthbear
 */
@Module.Info(name = "AntiHitbox", description = "Ignores hitboxes for placing and breaking blocks", category = Category.PLAYER)
public class AntiHitbox extends Module {
    @EventHandler
    public EventListener<AntiHitboxEvent> antiHitboxEvent = new EventListener<>(event -> {
        if(MC.interactionManager.isBreakingBlock()) event.setResult(Result.ALLOW);
    });
}
