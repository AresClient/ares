package dev.tigr.ares.fabric.impl.modules.player;

import dev.tigr.ares.core.feature.module.Category;
import dev.tigr.ares.core.feature.module.Module;
import dev.tigr.ares.core.util.global.ReflectionHelper;
import dev.tigr.ares.fabric.event.client.PacketEvent;
import dev.tigr.simpleevents.listener.EventHandler;
import dev.tigr.simpleevents.listener.EventListener;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;

/**
 * @author Tigermouthbear
 * updated to 1.16.2 on 9/5/20
 */
@Module.Info(name = "FakeRotation", description = "Spoofs player packet rotations", category = Category.PLAYER)
public class FakeRotation extends Module {
    @EventHandler
    public EventListener<PacketEvent.Sent> packetSentEvent = new EventListener<>(event -> {
        if(event.getPacket() instanceof PlayerMoveC2SPacket)
            ReflectionHelper.setPrivateValue(PlayerMoveC2SPacket.class, event.getPacket(), -90, "pitch", "field_12885");
    });
}
