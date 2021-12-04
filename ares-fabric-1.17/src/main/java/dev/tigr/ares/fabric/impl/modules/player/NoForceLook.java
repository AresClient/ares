package dev.tigr.ares.fabric.impl.modules.player;

import dev.tigr.ares.core.feature.module.Category;
import dev.tigr.ares.core.feature.module.Module;
import dev.tigr.ares.fabric.event.client.PacketEvent;
import dev.tigr.ares.fabric.mixin.accessors.PlayerPositionLookS2CPacketAccessor;
import dev.tigr.simpleevents.listener.EventHandler;
import dev.tigr.simpleevents.listener.EventListener;
import net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket;

/**
 * @author Tigermouthbear
 * updated to 1.17 on 7/3/21
 */
@Module.Info(name = "NoForceLook", description = "Discards server rotation packets", category = Category.PLAYER)
public class NoForceLook extends Module {
    @EventHandler
    public EventListener<PacketEvent.Receive> packetReceiveEvent = new EventListener<>(event -> {
        if(MC.player == null || MC.world == null) return;
        if(event.getPacket() instanceof PlayerPositionLookS2CPacket) {
            PlayerPositionLookS2CPacket packet = (PlayerPositionLookS2CPacket) event.getPacket();
            ((PlayerPositionLookS2CPacketAccessor) packet).setPitch(MC.player.getPitch());
            ((PlayerPositionLookS2CPacketAccessor) packet).setYaw(MC.player.getYaw());
        }
    });
}
