package dev.tigr.ares.fabric.impl.modules.combat;

import dev.tigr.ares.core.feature.module.Category;
import dev.tigr.ares.core.feature.module.Module;
import dev.tigr.ares.fabric.event.client.PacketEvent;
import dev.tigr.simpleevents.listener.EventHandler;
import dev.tigr.simpleevents.listener.EventListener;
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;

/**
 * @author Tigermouthbear 7/10/20
 */
@Module.Info(name = "Criticals", description = "Allows you to always hit enemies with critical damage", category = Category.COMBAT)
public class Criticals extends Module {
    @EventHandler
    public EventListener<PacketEvent.Sent> packetSentEvent = new EventListener<>(event -> {
        if(event.getPacket() instanceof PlayerInteractEntityC2SPacket && !MC.player.isInLava() && !MC.player.isSubmergedInWater() && MC.player.isOnGround()) {
            if(((PlayerInteractEntityC2SPacket) event.getPacket()).getType() == PlayerInteractEntityC2SPacket.InteractionType.ATTACK) {
                if(((PlayerInteractEntityC2SPacket) event.getPacket()).getEntity(MC.world) instanceof EndCrystalEntity)
                    return;
                MC.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.PositionOnly(MC.player.getX(), MC.player.getY() + 0.1f, MC.player.getZ(), false));
                MC.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.PositionOnly(MC.player.getX(), MC.player.getY(), MC.player.getZ(), false));
                Entity entity = ((PlayerInteractEntityC2SPacket) event.getPacket()).getEntity(MC.world);
                if(entity != null) MC.player.addCritParticles(entity);
            }
        }
    });
}
