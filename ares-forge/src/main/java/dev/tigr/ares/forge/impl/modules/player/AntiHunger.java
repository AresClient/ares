package dev.tigr.ares.forge.impl.modules.player;

import dev.tigr.ares.core.feature.module.Category;
import dev.tigr.ares.core.feature.module.Module;
import dev.tigr.ares.core.util.global.ReflectionHelper;
import dev.tigr.ares.forge.event.events.player.PacketEvent;
import dev.tigr.simpleevents.listener.EventHandler;
import dev.tigr.simpleevents.listener.EventListener;
import net.minecraft.network.play.client.CPacketPlayer;

/**
 * @author Tigermouthbear
 */
@Module.Info(name = "AntiHunger", description = "Prevents hunger", category = Category.PLAYER)
public class AntiHunger extends Module {
    @EventHandler
    public EventListener<PacketEvent.Sent> PacketSentEvent = new EventListener<>(event -> {
        if(event.getPacket() instanceof CPacketPlayer)
            ReflectionHelper.setPrivateValue(CPacketPlayer.class, (CPacketPlayer) event.getPacket(), false, "onGround", "field_149474_g");
    });
}