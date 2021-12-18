package dev.tigr.ares.fabric.impl.modules.movement;

import dev.tigr.ares.core.event.movement.EntityClipEvent;
import dev.tigr.ares.core.feature.module.Category;
import dev.tigr.ares.core.feature.module.Module;
import dev.tigr.simpleevents.listener.EventHandler;
import dev.tigr.simpleevents.listener.EventListener;

/**
 * @author Tigermouthbear
 */
@Module.Info(name = "NoClip", description = "Allows you to clip through walls", category = Category.MOVEMENT)
public class NoClip extends Module {
    @EventHandler
    public EventListener<EntityClipEvent> entityClipEvent = new EventListener<>(event -> {
        if(ENTITY.isSelf(event.getEntity())) event.setCancelled(true);
    });

    @Override
    public void onMotion() {
        MC.player.noClip = true;
        MC.player.fallDistance = 0f;

        MC.player.setOnGround(false);

        MC.player.upwardSpeed = 0.32f;

        if(MC.options.keyJump.isPressed()) MC.player.addVelocity(0, 0.32f, 0);
        if(MC.options.keySneak.isPressed()) MC.player.addVelocity(0, -0.32f, 0);
    }
}
