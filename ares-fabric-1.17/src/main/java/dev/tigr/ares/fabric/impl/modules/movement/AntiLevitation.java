package dev.tigr.ares.fabric.impl.modules.movement;

import dev.tigr.ares.core.feature.module.Category;
import dev.tigr.ares.core.feature.module.Module;
import dev.tigr.ares.fabric.event.player.StatusEffectEvent;
import dev.tigr.simpleevents.listener.EventHandler;
import dev.tigr.simpleevents.listener.EventListener;
import net.minecraft.entity.effect.StatusEffects;

/**
 * @author Tigermouthbear 12/14/20
 */
@Module.Info(name = "AntiLevitation", description = "Prevents levitation from the levitation effect", category = Category.MOVEMENT)
public class AntiLevitation extends Module {
    @EventHandler
    public EventListener<StatusEffectEvent> statusEffectEvent = new EventListener<>(event -> {
        if(event.getLivingEntity() == MC.player && event.getStatusEffect() == StatusEffects.LEVITATION) event.setCancelled(true);
    });
}
