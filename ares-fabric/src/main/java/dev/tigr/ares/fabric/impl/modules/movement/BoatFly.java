package dev.tigr.ares.fabric.impl.modules.movement;

import dev.tigr.ares.core.feature.module.Category;
import dev.tigr.ares.core.feature.module.Module;
import dev.tigr.ares.core.setting.Setting;
import dev.tigr.ares.core.setting.settings.BooleanSetting;
import dev.tigr.ares.core.setting.settings.numerical.DoubleSetting;
import dev.tigr.ares.core.setting.settings.numerical.IntegerSetting;
import dev.tigr.ares.fabric.event.client.PacketEvent;
import dev.tigr.ares.fabric.event.movement.BoatMoveEvent;
import dev.tigr.simpleevents.listener.EventHandler;
import dev.tigr.simpleevents.listener.EventListener;
import io.netty.buffer.Unpooled;

import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.c2s.play.*;
import net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket;
import net.minecraft.network.packet.s2c.play.VehicleMoveS2CPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Vec3d;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Tigermouthbear
 */
@Module.Info(name = "BoatFly", description = "Allows you to fly in boats", category = Category.MOVEMENT)
public class BoatFly extends Module {
    public static BoatFly INSTANCE;

    private final Setting<Double> speed = register(new DoubleSetting("Speed", 0.9D, 0.01D, 12.0D));
    private final Setting<Double> fallSpeed = register(new DoubleSetting("Fall Speed", 0, 0, 2));
    private final Setting<Boolean> bypass = register(new BooleanSetting("Bypass", false));
    private final Setting<Integer> interval = register(new IntegerSetting("Bypass Interval", 2, 1, 10)).setVisibility(bypass::getValue);
    private final Setting<Boolean> phase = register(new BooleanSetting("Phase", false));

    private final List<VehicleMoveC2SPacket> packets = new ArrayList<>();
    private BoatEntity boat = null;
    private int tpId = 0;

    public BoatFly() {
        INSTANCE = this;
    }

    @Override
    public void onDisable() {
        if(boat != null) boat.noClip = false;
        boat = null;
        packets.clear();
    }

    @EventHandler
    public EventListener<BoatMoveEvent> boatMoveEvent = new EventListener<>(event -> {
        if(event.getBoat() == null) {
            boat = null;
            return;
        }

        if(boat != event.getBoat()) boat = event.getBoat();

        event.y = 0;

        boat.yaw = MC.player.yaw;

        // set phase
        if(phase.getValue()) {
            boat.noClip = true;
            boat.pushSpeedReduction = 1;
        } else boat.noClip = false;

        if(!MC.options.keyForward.isPressed() &&
                !MC.options.keyBack.isPressed() &&
                !MC.options.keyLeft.isPressed() &&
                !MC.options.keyRight.isPressed()) event.x = event.z = 0;
        else {
            float yaw = MC.player.yaw;
            float forward = 1;

            if(MC.player.forwardSpeed < 0) {
                yaw += 180;
                forward = -0.5f;
            } else if(MC.player.forwardSpeed > 0) forward = 0.5f;

            if(MC.player.sidewaysSpeed > 0) yaw -= 90 * forward;
            if(MC.player.sidewaysSpeed < 0) yaw += 90 * forward;

            yaw = (float) Math.toRadians(yaw);

            event.x = -Math.sin(yaw) * speed.getValue();
            event.z = Math.cos(yaw) * speed.getValue();
        }

        if(MC.options.keySprint.isPressed()) event.y = -speed.getValue();
        else if(MC.options.keyJump.isPressed()) event.y += speed.getValue();
        else event.y = -fallSpeed.getValue();
    });

    @EventHandler
    public EventListener<PacketEvent.Receive> packetReceiveEvent = new EventListener<>(event -> {
        if(bypass.getValue() && MC.player != null && MC.player.isRiding()) {
            if(event.getPacket() instanceof PlayerPositionLookS2CPacket) {
                tpId = ((PlayerPositionLookS2CPacket) event.getPacket()).getTeleportId();

                event.setCancelled(true);
            }
            if(event.getPacket() instanceof VehicleMoveS2CPacket) event.setCancelled(true);
        }
    });

    private Vec3d getRidingPosition() {
        float f = 0.0F;
        float f1 = (float)((!boat.isAlive() ? 0.009999999776482582D : boat.getMountedHeightOffset()) + MC.player.getHeightOffset());

        if(boat.getPassengerList().size() > 1) {
            if(boat.getPassengerList().indexOf(MC.player) == 0) f = 0.2F;
            else f = -0.6F;
        }

        Vec3d vector = (new Vec3d(f, 0.0D, 0.0D)).rotateX(-boat.yaw * 0.017453292F - ((float)Math.PI / 2F));
        return new Vec3d(boat.getX() + vector.x, boat.getY() + (double)f1, boat.getZ() + vector.z);
    }

    @EventHandler
    public EventListener<PacketEvent.Sent> packetSentEvent = new EventListener<>(event -> {
        if(bypass.getValue() && MC.player != null && MC.player.isRiding() && boat != null) {
            if(event.getPacket() instanceof VehicleMoveC2SPacket && TICKS % interval.getValue() == 0) {
                if(packets.contains(event.getPacket())) packets.remove(event.getPacket());
                else {
                    // move boat to previous location
                    try {
                        event.getPacket().read(createPacketData(boat.prevX, boat.prevY, boat.prevZ, boat.prevYaw, boat.prevPitch));
                    } catch(IOException e) {
                        UTILS.printMessage("Error while using boatfly, disabling...");
                        setEnabled(false);
                    }

                    // cause dismount then remount
                    MC.player.networkHandler.sendPacket(new PlayerInteractEntityC2SPacket(boat, Hand.OFF_HAND, false));

                    // move boat to current location
                    MC.player.networkHandler.sendPacket(add(new VehicleMoveC2SPacket(boat)));

                    // send player position
                    Vec3d pos = getRidingPosition();
                    MC.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.PositionOnly(pos.x, pos.y, pos.z, MC.player.isOnGround()));

                    // confirm with predicted tpId
                    ++tpId;
                    MC.player.networkHandler.sendPacket(new TeleportConfirmC2SPacket(tpId));
                }
            }

            if(event.getPacket() instanceof PlayerInputC2SPacket || event.getPacket() instanceof PlayerMoveC2SPacket.LookOnly || event.getPacket() instanceof BoatPaddleStateC2SPacket) event.setCancelled(true);
        }
    });

    private VehicleMoveC2SPacket createPacket(double x, double y, double z, float yaw, float pitch) {
        VehicleMoveC2SPacket packet = new VehicleMoveC2SPacket();

        try {
            packet.read(createPacketData(x, y, z, yaw, pitch));
        } catch(IOException e) {
            UTILS.printMessage("Error while using boatfly, disabling...");
            setEnabled(false);
        }

        return packet;
    }

    private PacketByteBuf createPacketData(double x, double y, double z, float yaw, float pitch) {
        PacketByteBuf buffer = new PacketByteBuf(Unpooled.buffer());

        buffer.writeDouble(x);
        buffer.writeDouble(y);
        buffer.writeDouble(z);
        buffer.writeFloat(yaw);
        buffer.writeFloat(pitch);

        return buffer;
    }

    private <T extends VehicleMoveC2SPacket> T add(T packet) {
        packets.add(packet);
        return packet;
    }
}
