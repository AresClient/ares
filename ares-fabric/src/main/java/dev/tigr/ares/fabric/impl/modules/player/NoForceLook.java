package dev.tigr.ares.fabric.impl.modules.player;

import dev.tigr.ares.core.feature.module.Category;
import dev.tigr.ares.core.feature.module.Module;
import dev.tigr.ares.core.util.global.ReflectionHelper;
import dev.tigr.ares.fabric.event.client.PacketEvent;
import dev.tigr.simpleevents.listener.EventHandler;
import dev.tigr.simpleevents.listener.EventListener;
import net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket;

/**
 * @author Tigermouthbear
 * updated to 1.16.2 on 9/5/20
 */
@Module.Info(name = "NoForceLook", description = "Discards server rotation packets", category = Category.PLAYER)
public class NoForceLook extends Module {
    @EventHandler
    public EventListener<PacketEvent.Receive> packetReceiveEvent = new EventListener<>(event -> {
        if(event.getPacket() instanceof PlayerPositionLookS2CPacket) {
            PlayerPositionLookS2CPacket packet = (PlayerPositionLookS2CPacket) event.getPacket();

            ReflectionHelper.setPrivateValue(PlayerPositionLookS2CPacket.class, packet, MC.player.pitch, "pitch", "field_12391");
            ReflectionHelper.setPrivateValue(PlayerPositionLookS2CPacket.class, packet, MC.player.yaw, "yaw", "field_12393");
        }
    });
}
