package dev.tigr.ares.forge.impl.modules.movement;

import dev.tigr.ares.core.feature.module.Category;
import dev.tigr.ares.core.feature.module.Module;
import dev.tigr.ares.core.setting.Setting;
import dev.tigr.ares.core.setting.settings.numerical.FloatSetting;
import dev.tigr.ares.core.util.global.ReflectionHelper;
import dev.tigr.ares.forge.event.events.movement.EntityPushEvent;
import dev.tigr.ares.forge.event.events.player.PacketEvent;
import dev.tigr.simpleevents.listener.EventHandler;
import dev.tigr.simpleevents.listener.EventListener;
import net.minecraft.network.play.server.SPacketEntityVelocity;
import net.minecraft.network.play.server.SPacketExplosion;

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
        if(event.getPacket() instanceof SPacketEntityVelocity) {
            SPacketEntityVelocity velocity = (SPacketEntityVelocity) event.getPacket();
            if(velocity.getEntityID() == MC.player.getEntityId()) {
                ReflectionHelper.setPrivateValue(SPacketEntityVelocity.class, velocity, (int) (velocity.getMotionX() * horizontal.getValue()), "motionX", "field_149415_b");
                ReflectionHelper.setPrivateValue(SPacketEntityVelocity.class, velocity, (int) (velocity.getMotionY() * vertical.getValue()), "motionY", "field_149416_c");
                ReflectionHelper.setPrivateValue(SPacketEntityVelocity.class, velocity, (int) (velocity.getMotionZ() * horizontal.getValue()), "motionZ", "field_149414_d");
            }
        } else if(event.getPacket() instanceof SPacketExplosion) {
            SPacketExplosion velocity = (SPacketExplosion) event.getPacket();
            ReflectionHelper.setPrivateValue(SPacketExplosion.class, velocity, (int) (velocity.getMotionX() * horizontal.getValue()), "motionX", "field_149152_f");
            ReflectionHelper.setPrivateValue(SPacketExplosion.class, velocity, (int) (velocity.getMotionY() * vertical.getValue()), "motionY", "field_149153_g");
            ReflectionHelper.setPrivateValue(SPacketExplosion.class, velocity, (int) (velocity.getMotionZ() * horizontal.getValue()), "motionZ", "field_149159_h");
        }
    });
}
