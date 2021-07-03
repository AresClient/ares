package dev.tigr.ares.fabric.impl.modules.combat;

import dev.tigr.ares.core.feature.module.Category;
import dev.tigr.ares.core.feature.module.Module;
import dev.tigr.ares.core.util.Pair;
import dev.tigr.ares.fabric.event.client.PacketEvent;
import dev.tigr.ares.fabric.utils.WorldUtils;
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
            Pair<WorldUtils.InteractType, Integer> interactData = WorldUtils.getInteractData((PlayerInteractEntityC2SPacket) event.getPacket());
            if(interactData.getFirst() == WorldUtils.InteractType.ATTACK) {
                Entity entity = MC.world.getEntityById(interactData.getSecond());
                if(entity instanceof EndCrystalEntity) return;

                MC.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(MC.player.getX(), MC.player.getY() + 0.1f, MC.player.getZ(), false));
                MC.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(MC.player.getX(), MC.player.getY(), MC.player.getZ(), false));
                if(entity != null) MC.player.addCritParticles(entity);
            }
        }
    });
}
