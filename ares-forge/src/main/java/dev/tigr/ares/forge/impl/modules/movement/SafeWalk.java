package dev.tigr.ares.forge.impl.modules.movement;

import dev.tigr.ares.core.feature.module.Category;
import dev.tigr.ares.core.feature.module.Module;
import dev.tigr.ares.forge.event.events.movement.WalkOffLedgeEvent;
import dev.tigr.simpleevents.listener.EventHandler;
import dev.tigr.simpleevents.listener.EventListener;
import net.minecraft.entity.MoverType;
import net.minecraft.entity.player.EntityPlayer;

/**
 * @author Tigermouthbear
 */
@Module.Info(name = "SafeWalk", description = "Keeps you from walking off ledges", category = Category.MOVEMENT)
public class SafeWalk extends Module {
    @EventHandler
    public EventListener<WalkOffLedgeEvent> walkOffLedgeEvent = new EventListener<>(event -> event.setCancelled(true));
}
