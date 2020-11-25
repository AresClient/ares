package dev.tigr.ares.forge.impl.modules.player;

import dev.tigr.ares.core.feature.module.Category;
import dev.tigr.ares.core.feature.module.Module;
import dev.tigr.ares.core.util.global.ReflectionHelper;
import dev.tigr.ares.forge.event.events.player.PacketEvent;
import dev.tigr.simpleevents.listener.EventHandler;
import dev.tigr.simpleevents.listener.EventListener;
import net.minecraft.network.play.server.SPacketPlayerPosLook;

/**
 * @author Tigermouthbear
 */
@Module.Info(name = "NoForceLook", description = "Discards server rotation packets", category = Category.PLAYER)
public class NoForceLook extends Module {
    @EventHandler
    public EventListener<PacketEvent.Receive> packetReceiveEvent = new EventListener<>(event -> {
        if(event.getPacket() instanceof SPacketPlayerPosLook) {
            SPacketPlayerPosLook packet = (SPacketPlayerPosLook) event.getPacket();

            ReflectionHelper.setPrivateValue(SPacketPlayerPosLook.class, packet, MC.player.rotationPitch, "pitch", "field_148937_e");
            ReflectionHelper.setPrivateValue(SPacketPlayerPosLook.class, packet, MC.player.rotationYaw, "yaw", "field_148936_d");
        }
    });
}
