package dev.tigr.ares.fabric.impl.modules.render;

import dev.tigr.ares.core.feature.module.Category;
import dev.tigr.ares.core.feature.module.Module;
import dev.tigr.ares.core.setting.Setting;
import dev.tigr.ares.core.setting.settings.BooleanSetting;
import dev.tigr.ares.fabric.event.client.PacketEvent;
import dev.tigr.simpleevents.listener.EventHandler;
import dev.tigr.simpleevents.listener.EventListener;
import net.minecraft.network.packet.s2c.play.ExplosionS2CPacket;
import net.minecraft.network.packet.s2c.play.ParticleS2CPacket;

/**
 * @author Tigermouthbear
 */
@Module.Info(name = "NoRender", description = "Stops explosions and particles from rendering", category = Category.RENDER)
public class NoRender extends Module {
    private final Setting<Boolean> stopExplosions = register(new BooleanSetting("Stop Explosions", true));
    private final Setting<Boolean> stopParticles = register(new BooleanSetting("Stop Particles", true));

    @EventHandler
    public EventListener<PacketEvent.Receive> packetReceiveEvent = new EventListener<>(event -> {
        if(stopExplosions.getValue() && event.getPacket() instanceof ExplosionS2CPacket) {
            event.setCancelled(true);
        }

        if(stopParticles.getValue() && event.getPacket() instanceof ParticleS2CPacket) {
            event.setCancelled(true);
        }
    });
}
