package dev.tigr.ares.forge.impl.modules.misc;

import dev.tigr.ares.core.feature.module.Category;
import dev.tigr.ares.core.feature.module.Module;
import dev.tigr.ares.forge.event.events.optimizations.InfiniteChat;
import dev.tigr.simpleevents.event.Result;
import dev.tigr.simpleevents.listener.EventHandler;
import dev.tigr.simpleevents.listener.EventListener;

/**
 * @author Tigermouthbear
 */
@Module.Info(name = "InfiniteChatLength", description = "Allows you to scroll infinitely back in the chat", category = Category.MISC, visible = false, enabled = true)
public class InfiniteChatLength extends Module {
    @EventHandler
    public EventListener<InfiniteChat> infiniteChatEvent = new EventListener<>(event -> {
        event.setResult(Result.ALLOW);
    });
}