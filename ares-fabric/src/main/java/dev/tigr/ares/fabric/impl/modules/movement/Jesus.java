package dev.tigr.ares.fabric.impl.modules.movement;

import dev.tigr.ares.core.feature.module.Category;
import dev.tigr.ares.core.feature.module.Module;
import dev.tigr.ares.core.setting.Setting;
import dev.tigr.ares.core.setting.settings.numerical.DoubleSetting;
import dev.tigr.ares.core.util.global.ReflectionHelper;
import dev.tigr.ares.fabric.event.client.PacketEvent;
import dev.tigr.ares.fabric.event.movement.WaterCollisionEvent;
import dev.tigr.simpleevents.listener.EventHandler;
import dev.tigr.simpleevents.listener.EventListener;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.util.shape.VoxelShapes;

/**
 * @author Tigermouthbear
 */
@Module.Info(name = "Jesus", description = "Walk on water", category = Category.MOVEMENT)
public class Jesus extends Module {
    private final Setting<Double> offset = register(new DoubleSetting("Offset", 0.1, 0, 0.9));

    @EventHandler
    public EventListener<WaterCollisionEvent> waterCollisionEvent = new EventListener<>(event -> {
        if(shouldJesus() && event.getPos().getY() < MC.player.getPos().y - offset.getValue())
            event.setShape(VoxelShapes.fullCube());
    });

    // tells the server were at an offset when we send a position packet
    @EventHandler
    public EventListener<PacketEvent.Sent> packetSentEvent = new EventListener<>(event -> {
        if(event.getPacket() instanceof PlayerMoveC2SPacket && MC.player.getVehicle() == null) {
            if(shouldJesus() && !MC.player.isSubmergedInWater() && MC.world.getBlockState(MC.player.getBlockPos().down()).getMaterial().isLiquid())
                ReflectionHelper.setPrivateValue(PlayerMoveC2SPacket.class, event.getPacket(), ((PlayerMoveC2SPacket) event.getPacket()).getY(0) - offset.getValue(), "y", "field_12886");
        }
    });

    private boolean shouldJesus() {
        return MC.player != null
                && !MC.player.isSneaking()
                && MC.player.fallDistance < 3.0f;
    }
}
