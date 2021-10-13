package dev.tigr.ares.forge.impl.modules.player;

import dev.tigr.ares.core.feature.module.Category;
import dev.tigr.ares.core.feature.module.Module;
import dev.tigr.ares.core.setting.Setting;
import dev.tigr.ares.core.setting.settings.BooleanSetting;
import dev.tigr.ares.core.setting.settings.numerical.FloatSetting;
import dev.tigr.ares.forge.event.events.movement.SendMovementPacketsEvent;
import dev.tigr.ares.forge.event.events.player.PacketEvent;
import dev.tigr.ares.forge.utils.WorldUtils;
import dev.tigr.simpleevents.listener.EventHandler;
import dev.tigr.simpleevents.listener.EventListener;
import net.minecraft.client.entity.EntityOtherPlayerMP;
import net.minecraft.entity.Entity;
import net.minecraft.network.play.client.CPacketInput;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraftforge.event.entity.living.LivingEvent;

/**
 * @author Tigermouthbear
 */
@Module.Info(name = "Freecam", description = "Allows you to view the world from a free moving camera", category = Category.PLAYER)
public class Freecam extends Module {
    public static Freecam INSTANCE;

    private final Setting<Float> speed = register(new FloatSetting("Speed", 1, 0.1f, 4));
    private final Setting<Boolean> cancelPackets = register(new BooleanSetting("Cancel Packets", true));

    public EntityOtherPlayerMP clone;
    private Entity ride = null;

    public Freecam() {
        INSTANCE = this;
    }

    @EventHandler
    public EventListener<PacketEvent.Sent> packetSentEvent = new EventListener<>(event -> {
        if((event.getPacket() instanceof CPacketInput) && cancelPackets.getValue())
            event.setCancelled(true);
    });

    @EventHandler
    private final EventListener<SendMovementPacketsEvent.Pre> onMovementPacketSent = new EventListener<>(event -> {
        if(cancelPackets.getValue()) event.setCancelled(true);
    });

    @EventHandler
    public EventListener<LivingEvent.LivingUpdateEvent> livingUpdateEvent = new EventListener<>(event -> MC.player.noClip = true);

    @Override
    public void onEnable() {
        if(MC.player.isRiding()) {
            ride = MC.player.getRidingEntity();
            MC.player.dismountRidingEntity();
        }
        clone = new EntityOtherPlayerMP(MC.world, MC.player.getGameProfile());
        clone.copyLocationAndAnglesFrom(MC.player);
        MC.world.spawnEntity(clone);
        MC.player.noClip = true;
        MC.renderChunksMany = false;
    }

    @Override
    public void onTick() {
        MC.player.noClip = true;
        MC.player.setVelocity(0, 0, 0);
        WorldUtils.moveEntityWithSpeed(MC.player, speed.getValue(), true);
    }

    @Override
    public void onDisable() {
        MC.renderChunksMany = true;
        MC.player.setVelocity(0, 0, 0);
        MC.player.noClip = false;
        if(clone != null) {
            MC.player.copyLocationAndAnglesFrom(clone);
            MC.world.removeEntity(clone);
        }
        if(ride != null) {
            MC.player.startRiding(ride);
            ride = null;
        }
    }
}
