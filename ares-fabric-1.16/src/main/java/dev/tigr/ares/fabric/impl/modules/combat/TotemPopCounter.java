package dev.tigr.ares.fabric.impl.modules.combat;

import dev.tigr.ares.core.feature.module.Category;
import dev.tigr.ares.core.feature.module.Module;
import dev.tigr.ares.core.util.render.TextColor;
import dev.tigr.ares.fabric.event.client.LivingDeathEvent;
import dev.tigr.ares.fabric.event.client.PacketEvent;
import dev.tigr.simpleevents.listener.EventHandler;
import dev.tigr.simpleevents.listener.EventListener;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.s2c.play.EntityStatusS2CPacket;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * @author Tigermouthbear 7/13/20
 * ported to Fabric by Hoosiers 11/26/20
 */
@Module.Info(name = "TotemPopCounter", description = "Counts the amount of totem pops a player has had in chat", category = Category.COMBAT)
public class TotemPopCounter extends Module {
    // name, count
    private final Map<String, Integer> popMap = new HashMap<>();

    @EventHandler
    public EventListener<PacketEvent.Receive> packetReceiveEvent = new EventListener<>(event -> {
        if(MC.player != null && event.getPacket() instanceof EntityStatusS2CPacket) {
            EntityStatusS2CPacket status = (EntityStatusS2CPacket) event.getPacket();
            if(status.getStatus() == 35) {
                Entity entity = status.getEntity(MC.world);
                if(!(entity instanceof PlayerEntity) || entity == MC.player) return;

                int amount = popMap.getOrDefault(entity.getEntityName(), 0) + 1;
                popMap.put(entity.getEntityName(), amount);
                UTILS.printMessage(TextColor.BLUE + entity.getEntityName() + TextColor.WHITE + " has popped " + amount + " totems");
            }
        }
    });

    @EventHandler
    public EventListener<LivingDeathEvent> deathEvent = new EventListener<>(event -> {
        if(popMap.containsKey(event.getEntity().getEntityName())) popMap.put(event.getEntity().getEntityName(), 0);
    });

    @Override
    public void onTick() {
        // only do this 2 times a second
        if(MC.player.age % 10 != 0) return;

        popMap.keySet().forEach(entity -> {
            Optional<AbstractClientPlayerEntity> optionalPlayerEntity = MC.world.getPlayers().stream().filter(playerEntity -> playerEntity.getEntityName().equals(entity)).findFirst();
            if(optionalPlayerEntity.isPresent()) {
                PlayerEntity player = optionalPlayerEntity.get();
                if(player.isDead() || player.getHealth() <= 0) popMap.put(entity, 0);
            }
        });
    }
}