package dev.tigr.ares.fabric.impl.modules.movement;

import dev.tigr.ares.core.feature.module.Category;
import dev.tigr.ares.core.feature.module.Module;
import dev.tigr.ares.fabric.event.movement.BlockPushEvent;
import dev.tigr.simpleevents.listener.EventHandler;
import dev.tigr.simpleevents.listener.EventListener;

/**
 * @author Makrennel
 * This is seperated from burrow because burrow toggles off after use.
 */
@Module.Info(name = "NoBlockPush", description = "Prevents full blocks from pushing you out (eg. When burrowed).", category = Category.MOVEMENT)
public class NoBlockPush extends Module {
    public static NoBlockPush INSTANCE;

    public NoBlockPush() {
        INSTANCE = this;
    }

    @EventHandler
    public EventListener<BlockPushEvent> onBurrowPush = new EventListener<>(event -> {
        event.setCancelled(true);
    });
}