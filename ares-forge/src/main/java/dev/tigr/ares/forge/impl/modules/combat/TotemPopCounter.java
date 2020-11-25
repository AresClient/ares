package dev.tigr.ares.forge.impl.modules.combat;

import dev.tigr.ares.core.feature.module.Category;
import dev.tigr.ares.core.feature.module.Module;
import dev.tigr.ares.core.util.render.TextColor;
import dev.tigr.ares.forge.event.events.player.PacketEvent;
import dev.tigr.simpleevents.listener.EventHandler;
import dev.tigr.simpleevents.listener.EventListener;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.play.server.SPacketEntityStatus;
import net.minecraftforge.event.entity.living.LivingDeathEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * @author Tigermouthbear 7/13/20
 */
@Module.Info(name = "TotemPopCounter", description = "Counts the amount of totem pops a player has had in chat", category = Category.COMBAT)
public class TotemPopCounter extends Module {
    // name, count
    private final Map<String, Integer> popMap = new HashMap<>();

    @EventHandler
    public EventListener<PacketEvent.Receive> packetReceiveEvent = new EventListener<>(event -> {
        if(MC.player != null && event.getPacket() instanceof SPacketEntityStatus) {
            SPacketEntityStatus status = (SPacketEntityStatus) event.getPacket();
            if(status.getOpCode() == 35) {
                Entity entity = status.getEntity(MC.world);
                if(!(entity instanceof EntityPlayer) || entity == MC.player) return;

                int amount = popMap.getOrDefault(entity.getName(), 0) + 1;
                popMap.put(entity.getName(), amount);
                UTILS.printMessage(TextColor.BLUE + entity.getName() + TextColor.WHITE + " has popped " + amount + " totems");
            }
        }
    });

    @EventHandler
    public EventListener<LivingDeathEvent> deathEvent = new EventListener<>(event -> {
        if(popMap.containsKey(event.getEntity().getName())) popMap.put(event.getEntity().getName(), 0);
    });

    @Override
    public void onTick() {
        // only do this 2 times a second
        if(MC.player.ticksExisted % 10 != 0) return;

        popMap.keySet().forEach(entity -> {
            Optional<EntityPlayer> optionalEntityPlayer = MC.world.playerEntities.stream().filter(entityPlayer -> entityPlayer.getName().equals(entity)).findFirst();
            if(optionalEntityPlayer.isPresent()) {
                EntityPlayer player = optionalEntityPlayer.get();
                if(player.isDead || player.getHealth() <= 0) popMap.put(entity, 0);
            }
        });
    }
}
