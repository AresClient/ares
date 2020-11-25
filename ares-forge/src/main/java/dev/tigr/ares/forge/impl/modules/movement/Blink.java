package dev.tigr.ares.forge.impl.modules.movement;

import dev.tigr.ares.core.feature.module.Category;
import dev.tigr.ares.core.feature.module.Module;
import dev.tigr.ares.forge.event.events.player.PacketEvent;
import dev.tigr.simpleevents.listener.EventHandler;
import dev.tigr.simpleevents.listener.EventListener;
import net.minecraft.client.entity.EntityOtherPlayerMP;
import net.minecraft.network.play.client.CPacketPlayer;

import java.util.LinkedList;
import java.util.Queue;

/**
 * @author Tigermouthbear
 */
@Module.Info(name = "Blink", description = "Choke packets sent to server so you can move without anyone seeing", category = Category.MOVEMENT, alwaysListening = true)
public class Blink extends Module {
    private final Queue<CPacketPlayer> queue = new LinkedList<>();
    private EntityOtherPlayerMP clone;

    @EventHandler
    public EventListener<PacketEvent.Sent> onPacketSent = new EventListener<>(event -> {
        if(getEnabled() && event.getPacket() instanceof CPacketPlayer) {
            event.setCancelled(true);
            queue.add((CPacketPlayer) event.getPacket());
        }
    });

    @Override
    public void onEnable() {
        if(MC.player != null) {
            clone = new EntityOtherPlayerMP(MC.world, MC.getSession().getProfile());
            clone.copyLocationAndAnglesFrom(MC.player);
            MC.world.addEntityToWorld(-69, clone);
        }
    }

    @Override
    public void onDisable() {
        while(!queue.isEmpty()) MC.player.connection.sendPacket(queue.poll());

        if(MC.player != null) {
            MC.world.removeEntityFromWorld(-69);
            clone = null;
        }
    }

    @Override
    public String getInfo() {
        return String.valueOf(queue.size());
    }
}
