package dev.tigr.ares.fabric.impl.modules.player;

import dev.tigr.ares.core.feature.module.Category;
import dev.tigr.ares.core.feature.module.Module;
import dev.tigr.ares.core.setting.Setting;
import dev.tigr.ares.core.setting.settings.BooleanSetting;
import dev.tigr.ares.core.setting.settings.numerical.FloatSetting;
import dev.tigr.ares.fabric.event.client.PacketEvent;
import dev.tigr.ares.fabric.event.movement.EntityClipEvent;
import dev.tigr.ares.fabric.event.player.ChangePoseEvent;
import dev.tigr.ares.fabric.utils.CopiedOtherClientPlayerEntity;
import dev.tigr.ares.fabric.utils.WorldUtils;
import dev.tigr.simpleevents.listener.EventHandler;
import dev.tigr.simpleevents.listener.EventListener;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityPose;
import net.minecraft.network.packet.c2s.play.PlayerInputC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;

/**
 * @author Tigermouthbear
 * updated to 1.16.2 on 9/5/20
 */
@Module.Info(name = "Freecam", description = "Allows you to view the world from a free moving camera", category = Category.PLAYER)
public class Freecam extends Module {
    public static Freecam INSTANCE;

    private final Setting<Float> speed = register(new FloatSetting("Speed", 1, 0.1f, 4));
    private final Setting<Boolean> cancelPackets = register(new BooleanSetting("Cancel Packets", true));

    public CopiedOtherClientPlayerEntity clone;
    private Entity ride = null;

    public Freecam() {
        INSTANCE = this;
    }

    @EventHandler
    public EventListener<PacketEvent.Sent> packetSentEvent = new EventListener<>(event -> {
        if((event.getPacket() instanceof PlayerMoveC2SPacket || event.getPacket() instanceof PlayerInputC2SPacket) && cancelPackets.getValue())
            event.setCancelled(true);
    });

    @Override
    public void onEnable() {
        if(MC.player.isRiding()) {
            ride = MC.player.getVehicle();
            MC.player.stopRiding();
        }
        clone = new CopiedOtherClientPlayerEntity(MC.world, MC.player);
        MC.world.addEntity(clone.getId(), clone);
        MC.chunkCullingEnabled = false;
    }

    @Override
    public void onTick() {
        WorldUtils.moveEntityWithSpeed(MC.player, speed.getValue(), true);
    }

    @Override
    public void onMotion() {
        MC.player.noClip = true;
        MC.player.setPose(EntityPose.STANDING);
    }

    @EventHandler
    public EventListener<EntityClipEvent> entityClipEvent = new EventListener<>(event -> {
        if(event.getEntity() == MC.player) event.setCancelled(true);
    });

    @EventHandler
    public EventListener<ChangePoseEvent> changePoseEvent = new EventListener<>(event -> event.setPose(EntityPose.STANDING));

    @Override
    public void onDisable() {
        MC.chunkCullingEnabled = true;
        MC.player.setVelocity(0, 0, 0);
        MC.player.noClip = false;
        if(clone != null) {
            MC.player.copyFrom(clone);
            MC.world.removeEntity(clone.getId(), Entity.RemovalReason.DISCARDED);
        }
        if(ride != null) {
            MC.player.startRiding(ride);
            ride = null;
        }
    }
}
