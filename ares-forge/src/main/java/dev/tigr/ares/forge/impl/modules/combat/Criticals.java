package dev.tigr.ares.forge.impl.modules.combat;

import dev.tigr.ares.core.feature.module.Category;
import dev.tigr.ares.core.feature.module.Module;
import dev.tigr.ares.forge.event.events.player.PacketEvent;
import dev.tigr.simpleevents.listener.EventHandler;
import dev.tigr.simpleevents.listener.EventListener;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityEnderCrystal;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.network.play.client.CPacketUseEntity;

/**
 * @author Tigermouthbear 7/10/20
 */
@Module.Info(name = "Criticals", description = "Allows you to always hit enemies with critical damage", category = Category.COMBAT)
public class Criticals extends Module {
    @EventHandler
    public EventListener<PacketEvent.Sent> packetSentEvent = new EventListener<>(event -> {
        if(event.getPacket() instanceof CPacketUseEntity && !MC.player.isInLava() && !MC.player.isInWater() && MC.player.onGround) {
            if(((CPacketUseEntity) event.getPacket()).getAction() == CPacketUseEntity.Action.ATTACK) {
                if(((CPacketUseEntity) event.getPacket()).getEntityFromWorld(MC.world) instanceof EntityEnderCrystal)
                    return;
                MC.player.connection.sendPacket(new CPacketPlayer.Position(MC.player.posX, MC.player.posY + 0.1f, MC.player.posZ, false));
                MC.player.connection.sendPacket(new CPacketPlayer.Position(MC.player.posX, MC.player.posY, MC.player.posZ, false));
                Entity entity = ((CPacketUseEntity) event.getPacket()).getEntityFromWorld(MC.world);
                if(entity != null) MC.player.onCriticalHit(entity);
            }
        }
    });
}
