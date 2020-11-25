package dev.tigr.ares.forge.impl.modules.movement;

import dev.tigr.ares.core.feature.module.Category;
import dev.tigr.ares.core.feature.module.Module;
import dev.tigr.simpleevents.listener.EventHandler;
import dev.tigr.simpleevents.listener.EventListener;
import net.minecraftforge.event.entity.living.LivingEvent;

/**
 * @author Tigermouthbear
 */
@Module.Info(name = "NoClip", description = "Allows you to clip through walls", category = Category.MOVEMENT)
public class NoClip extends Module {
    @EventHandler
    public EventListener<LivingEvent.LivingUpdateEvent> livingUpdateEvent = new EventListener<>(event -> MC.player.noClip = true);

    @Override
    public void onTick() {
        MC.player.noClip = true;
        MC.player.fallDistance = 0f;

        MC.player.onGround = false;

        MC.player.jumpMovementFactor = 0.32f;

        if(MC.gameSettings.keyBindJump.isKeyDown()) MC.player.motionY += 0.32f;
        if(MC.gameSettings.keyBindSneak.isKeyDown()) MC.player.motionY -= 0.32f;
    }
}
