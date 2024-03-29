package dev.tigr.ares.forge.impl.modules.movement;

import dev.tigr.ares.core.feature.module.Category;
import dev.tigr.ares.core.feature.module.Module;
import dev.tigr.ares.forge.event.events.movement.BlockPushEvent;
import dev.tigr.ares.forge.event.events.movement.EntityPushEvent;
import dev.tigr.ares.forge.event.events.player.PacketEvent;
import dev.tigr.simpleevents.listener.EventHandler;
import dev.tigr.simpleevents.listener.EventListener;
import net.minecraft.network.play.server.SPacketEntityVelocity;
import net.minecraft.network.play.server.SPacketExplosion;

/**
 * @author Tigermouthbear
 */
@Module.Info(name = "Velocity", description = "Change knockback values", category = Category.MOVEMENT)
public class Velocity extends Module {

    @EventHandler
    public EventListener<BlockPushEvent> onBurrowPush = new EventListener<>(event -> event.setCancelled(true));

    @EventHandler
    public EventListener<EntityPushEvent> entityPushEvent = new EventListener<>(event -> {
        if(event.getEntity() == MC.player) event.setCancelled(true);
    });

    @EventHandler
    public EventListener<PacketEvent.Receive> packetReceiveEvent = new EventListener<>(event -> {
        if(event.getPacket() instanceof SPacketEntityVelocity) {
            SPacketEntityVelocity velocity = (SPacketEntityVelocity) event.getPacket();
            if(velocity.getEntityID() == MC.player.getEntityId()) {
                event.setCancelled(true);
            }
        } else if(event.getPacket() instanceof SPacketExplosion) {
            event.setCancelled(true);
        }
    });
}
