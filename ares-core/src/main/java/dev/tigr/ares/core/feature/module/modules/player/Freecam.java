package dev.tigr.ares.core.feature.module.modules.player;

import dev.tigr.ares.core.event.client.PacketEvent;
import dev.tigr.ares.core.event.movement.EntityClipEvent;
import dev.tigr.ares.core.event.movement.SendMovementPacketsEvent;
import dev.tigr.ares.core.event.player.ChangePoseEvent;
import dev.tigr.ares.core.feature.module.Category;
import dev.tigr.ares.core.feature.module.Module;
import dev.tigr.ares.core.setting.Setting;
import dev.tigr.ares.core.setting.settings.BooleanSetting;
import dev.tigr.ares.core.setting.settings.numerical.FloatSetting;
import dev.tigr.ares.core.util.entity.EntityUtils;
import dev.tigr.simpleevents.listener.EventHandler;
import dev.tigr.simpleevents.listener.EventListener;

/**
 * @author Tigermouthbear
 * updated to 1.16.2 on 9/5/20
 * moved to core 2021/12/16 - Makrennel
 */
@Module.Info(name = "Freecam", description = "Allows you to view the world from a free moving camera", category = Category.PLAYER)
public class Freecam extends Module {
    public static Freecam INSTANCE;

    private final Setting<Float> hSpeed = register(new FloatSetting("Horz. Speed", 1, 0.1f, 4));
    private final Setting<Float> vSpeed = register(new FloatSetting("Vert. Speed", 1, 0.1f, 4));
    private final Setting<Boolean> cancelPackets = register(new BooleanSetting("Cancel Packets", true));

    public Integer clone;
    private Integer ride = null;

    public Freecam() {
        INSTANCE = this;
    }

    @EventHandler
    public EventListener<PacketEvent.Sent.Input> packetSentEvent = new EventListener<>(event -> {
        if(cancelPackets.getValue()) event.setCancelled(true);
    });

    @EventHandler
    private final EventListener<SendMovementPacketsEvent.Pre> onMovementPacketSent = new EventListener<>(event -> {
        if(cancelPackets.getValue()) event.setCancelled(true);
    });

    @Override
    public void onEnable() {
        if(SELF.isRiding()) {
            ride = SELF.getRidingEntity();
            SELF.stopRiding();
        }
        clone = WORLD.createAndSpawnClone();
        WORLD.setChunkCulling(false);
    }

    @Override
    public void onTick() {
        EntityUtils.moveEntityWithSpeed(SELF.getId(), hSpeed.getValue(), true, vSpeed.getValue());
    }

    @Override
    public void onMotion() {
        SELF.setNoClip(true);
        SELF.setPose("STANDING");
    }

    @EventHandler
    public final EventListener<EntityClipEvent> entityClipEvent = new EventListener<>(event -> {
        if(ENTITY.isSelf(event.getEntity())) event.setCancelled(true);
    });

    @EventHandler
    public final EventListener<ChangePoseEvent> changePoseEvent = new EventListener<>(event -> event.setPose("STANDING"));

    @Override
    public void onDisable() {
        WORLD.setChunkCulling(false);
        SELF.setVelocity(0, 0, 0);
        SELF.setNoClip(false);
        if(clone != null) {
            SELF.copyFrom(clone);
            WORLD.removeEntity(clone);
        }
        if(ride != null) {
            SELF.startRiding(ride);
            ride = null;
        }
    }
}
