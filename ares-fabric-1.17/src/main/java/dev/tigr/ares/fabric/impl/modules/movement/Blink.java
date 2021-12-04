package dev.tigr.ares.fabric.impl.modules.movement;

import dev.tigr.ares.core.feature.module.Category;
import dev.tigr.ares.core.feature.module.Module;
import dev.tigr.ares.fabric.event.client.PacketEvent;
import dev.tigr.ares.fabric.utils.CopiedOtherClientPlayerEntity;
import dev.tigr.simpleevents.listener.EventHandler;
import dev.tigr.simpleevents.listener.EventListener;
import net.minecraft.entity.Entity;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;

import java.util.LinkedList;
import java.util.Queue;

/**
 * @author Tigermouthbear
 */
@Module.Info(name = "Blink", description = "Choke packets sent to server so you can move without anyone seeing", category = Category.MOVEMENT, alwaysListening = true)
public class Blink extends Module {
    private final Queue<PlayerMoveC2SPacket> queue = new LinkedList<>();
    private CopiedOtherClientPlayerEntity clone;

    @EventHandler
    public EventListener<PacketEvent.Sent> onPacketSent = new EventListener<>(event -> {
        if(getEnabled() && event.getPacket() instanceof PlayerMoveC2SPacket) {
            event.setCancelled(true);
            queue.add((PlayerMoveC2SPacket) event.getPacket());
        }
    });

    @Override
    public void onEnable() {
        if(MC.player != null) {
            clone = new CopiedOtherClientPlayerEntity(MC.world, MC.player);
            MC.world.addEntity(clone.getId(), clone);
        }
    }

    @Override
    public void onDisable() {
        while(!queue.isEmpty()) MC.player.networkHandler.sendPacket(queue.poll());

        if(MC.player != null) {
            MC.world.removeEntity(clone.getId(), Entity.RemovalReason.DISCARDED);
            clone = null;
        }
    }

    @Override
    public String getInfo() {
        return String.valueOf(queue.size());
    }
}
