package dev.tigr.ares.fabric.impl.modules.player;

import dev.tigr.ares.core.feature.module.Category;
import dev.tigr.ares.core.feature.module.Module;
import dev.tigr.ares.fabric.event.client.PacketEvent;
import dev.tigr.ares.fabric.mixin.accessors.PlayerMoveC2SPacketAccessor;
import dev.tigr.simpleevents.listener.EventHandler;
import dev.tigr.simpleevents.listener.EventListener;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;

/**
 * @author Tigermouthbear
 * updated to 1.17 on 7/3/21
 */
@Module.Info(name = "AntiHunger", description = "Prevents hunger", category = Category.PLAYER)
public class AntiHunger extends Module {
    @EventHandler
    public EventListener<PacketEvent.Sent> PacketSentEvent = new EventListener<>(event -> {
        if(event.getPacket() instanceof PlayerMoveC2SPacket)
            ((PlayerMoveC2SPacketAccessor) event.getPacket()).setOnGround(false);
    });
}