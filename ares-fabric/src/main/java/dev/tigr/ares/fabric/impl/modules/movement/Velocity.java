package dev.tigr.ares.fabric.impl.modules.movement;

import dev.tigr.ares.core.feature.module.Category;
import dev.tigr.ares.core.feature.module.Module;
import dev.tigr.ares.core.setting.Setting;
import dev.tigr.ares.core.setting.settings.numerical.FloatSetting;
import dev.tigr.ares.core.util.global.ReflectionHelper;
import dev.tigr.ares.fabric.event.client.PacketEvent;
import dev.tigr.ares.fabric.event.movement.EntityPushEvent;
import dev.tigr.simpleevents.listener.EventHandler;
import dev.tigr.simpleevents.listener.EventListener;
import net.minecraft.network.packet.s2c.play.EntityVelocityUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.ExplosionS2CPacket;

/**
 * @author Tigermouthbear
 */
@Module.Info(name = "Velocity", description = "Change knockback values", category = Category.MOVEMENT)
public class Velocity extends Module {
    private final Setting<Float> horizontal = register(new FloatSetting("Horizontal", 0, 0, 4));
    private final Setting<Float> vertical = register(new FloatSetting("Vertical", 0, 0, 1));

    @EventHandler
    public EventListener<EntityPushEvent> entityPushEvent = new EventListener<>(event -> {
        if(event.getEntity() == MC.player) event.setCancelled(true);
    });

    @EventHandler
    public EventListener<PacketEvent.Receive> packetReceiveEvent = new EventListener<>(event -> {
        if(MC.world == null || MC.player == null) return;
        if(event.getPacket() instanceof EntityVelocityUpdateS2CPacket) {
            EntityVelocityUpdateS2CPacket velocity = (EntityVelocityUpdateS2CPacket) event.getPacket();
            if(velocity.getId() == MC.player.getEntityId()) {
                ReflectionHelper.setPrivateValue(EntityVelocityUpdateS2CPacket.class, velocity, (int) (velocity.getVelocityX() * horizontal.getValue()), "velocityX", "field_12561");
                ReflectionHelper.setPrivateValue(EntityVelocityUpdateS2CPacket.class, velocity, (int) (velocity.getVelocityY() * vertical.getValue()), "velocityY", "field_12562");
                ReflectionHelper.setPrivateValue(EntityVelocityUpdateS2CPacket.class, velocity, (int) (velocity.getVelocityZ() * horizontal.getValue()), "velocityZ", "field_12563");
            }
        } else if(event.getPacket() instanceof ExplosionS2CPacket) {
            ExplosionS2CPacket velocity = (ExplosionS2CPacket) event.getPacket();
            ReflectionHelper.setPrivateValue(ExplosionS2CPacket.class, velocity, (int) (velocity.getPlayerVelocityX() * horizontal.getValue()), "playerVelocityX", "field_12176");
            ReflectionHelper.setPrivateValue(ExplosionS2CPacket.class, velocity, (int) (velocity.getPlayerVelocityY() * vertical.getValue()), "playerVelocityY", "field_12183");
            ReflectionHelper.setPrivateValue(ExplosionS2CPacket.class, velocity, (int) (velocity.getPlayerVelocityZ() * horizontal.getValue()), "playerVelocityZ", "field_12182");
        }
    });
}
