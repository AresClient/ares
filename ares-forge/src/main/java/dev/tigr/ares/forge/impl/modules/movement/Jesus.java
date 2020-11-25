package dev.tigr.ares.forge.impl.modules.movement;

import dev.tigr.ares.core.feature.module.Category;
import dev.tigr.ares.core.feature.module.Module;
import dev.tigr.ares.core.setting.Setting;
import dev.tigr.ares.core.setting.settings.EnumSetting;
import dev.tigr.ares.core.setting.settings.numerical.DoubleSetting;
import dev.tigr.ares.core.util.global.ReflectionHelper;
import dev.tigr.ares.forge.event.events.movement.WaterCollisionBoxEvent;
import dev.tigr.ares.forge.event.events.movement.WaterMoveEvent;
import dev.tigr.ares.forge.event.events.player.PacketEvent;
import dev.tigr.simpleevents.listener.EventHandler;
import dev.tigr.simpleevents.listener.EventListener;
import net.minecraft.block.Block;
import net.minecraft.network.play.client.CPacketPlayer;

/**
 * @author Tigermouthbear 10/2/20
 */
@Module.Info(name = "Jesus", description = "Walk on water", category = Category.MOVEMENT)
public class Jesus extends Module {
    enum Mode { NORMAL, BOUNCE, MOVE, FLIGHT }

    private final Setting<Mode> mode = register(new EnumSetting<>("Mode", Mode.NORMAL));
    private final Setting<Double> speed = register(new DoubleSetting("Speed", 1, 0, 20)).setVisibility(() -> mode.getValue() == Mode.FLIGHT || mode.getValue() == Mode.BOUNCE);
    private final Setting<Double> offset = register(new DoubleSetting("Offset", 0.1, 0, 0.9)).setVisibility(() -> mode.getValue() == Mode.NORMAL);

    @EventHandler
    public EventListener<WaterMoveEvent> waterMoveEvent = new EventListener<>(event -> {
        if(!shouldJesus()) return;
        if(mode.getValue() == Mode.FLIGHT) {
            event.setX(event.getX() * speed.getValue());
            event.setZ(event.getZ() * speed.getValue());

            if(MC.gameSettings.keyBindJump.isKeyDown()) event.setY(speed.getValue());
            else if(MC.gameSettings.keyBindSneak.isKeyDown()) event.setY(-speed.getValue());
            else event.setY(0);
        } else if(mode.getValue() == Mode.MOVE) event.setY(0);
    });

    @EventHandler
    public EventListener<WaterCollisionBoxEvent> waterCollisionBoxEvent = new EventListener<>(event -> {
        if(mode.getValue() != Mode.NORMAL) return;
        if(shouldJesus() && event.getPos().getY() < MC.player.posY - offset.getValue())
                event.setBoundingBox(Block.FULL_BLOCK_AABB);
    });

    // tells the server were at an offset when we send a position packet
    @EventHandler
    public EventListener<PacketEvent.Sent> packetSentEvent = new EventListener<>(event -> {
        if(mode.getValue() == Mode.NORMAL && event.getPacket() instanceof CPacketPlayer && MC.player.getRidingEntity() == null) {
            if(shouldJesus() && !MC.player.isInWater() && MC.world.getBlockState(MC.player.getPosition().down()).getMaterial().isLiquid() && MC.player.ticksExisted % 3 == 0)
                ReflectionHelper.setPrivateValue(CPacketPlayer.class, (CPacketPlayer) event.getPacket(), ((CPacketPlayer) event.getPacket()).getY(0) - offset.getValue(), "y", "field_149477_b");
        }
    });

    @Override
    public void onMotion() {
        if(mode.getValue() == Mode.BOUNCE && (MC.player.isInWater() || MC.player.isInWater()) && !MC.player.isSneaking()) {
            MC.player.motionY += speed.getValue() / 20;
            MC.player.motionY *= 1.2;
        }
    }

    private boolean shouldJesus() {
        return MC.player != null
                && !MC.player.isSneaking()
                && MC.player.fallDistance < 3.0f;
    }

    @Override
    protected String getInfo() {
        return mode.getValue().name();
    }
}
