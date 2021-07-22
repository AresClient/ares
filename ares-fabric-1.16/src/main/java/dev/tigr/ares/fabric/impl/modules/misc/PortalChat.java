package dev.tigr.ares.fabric.impl.modules.misc;

import dev.tigr.ares.core.event.render.PortalChatEvent;
import dev.tigr.ares.core.feature.module.Category;
import dev.tigr.ares.core.feature.module.Module;
import dev.tigr.simpleevents.listener.EventHandler;
import dev.tigr.simpleevents.listener.EventListener;

/**
 * @author Tigermouthbear
 */
@Module.Info(name = "PortalChat", description = "Allows you to open guis while in nether portals", category = Category.MISC)
public class PortalChat extends Module {
    @EventHandler
    public EventListener<PortalChatEvent> portalChatEvent = new EventListener<>(event -> event.setCancelled(true));
}
