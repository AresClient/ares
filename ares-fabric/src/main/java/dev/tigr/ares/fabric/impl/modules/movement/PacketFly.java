package dev.tigr.ares.fabric.impl.modules.movement;

import dev.tigr.ares.core.feature.module.Category;
import dev.tigr.ares.core.feature.module.Module;
import dev.tigr.ares.core.setting.Setting;
import dev.tigr.ares.core.setting.settings.BooleanSetting;
import dev.tigr.ares.core.setting.settings.EnumSetting;
import dev.tigr.ares.core.setting.settings.numerical.DoubleSetting;
import dev.tigr.ares.fabric.event.client.PacketEvent;
import dev.tigr.ares.fabric.event.movement.MovePlayerEvent;
import dev.tigr.ares.fabric.mixin.accessors.PlayerMoveC2SPacketAccessor;
import dev.tigr.ares.fabric.mixin.accessors.PlayerPositionLookS2CPacketAccessor;
import dev.tigr.simpleevents.listener.EventHandler;
import dev.tigr.simpleevents.listener.EventListener;
import net.minecraft.block.Blocks;
import net.minecraft.network.packet.c2s.play.CloseHandledScreenC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.network.packet.c2s.play.TeleportConfirmC2SPacket;
import net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Tigermouthbear
 */
@Module.Info(name = "PacketFly", description = "Fly using packets", category = Category.MOVEMENT)
public class PacketFly extends Module {
    enum Mode { SETBACK, FAST }
    enum Bounds { DOWN, GROUND, UP }
    private final Setting<Mode> mode = register(new EnumSetting<>("Mode", Mode.SETBACK));
    private final Setting<Bounds> bounds = register(new EnumSetting<>("Bounds", Bounds.DOWN));
    private final Setting<Double> speed = register(new DoubleSetting("Speed", 0.2, 0.01, 0.5));
    private final Setting<Double> upSpeed = register(new DoubleSetting("Up Speed", 0.05, 0.01, 0.2));
    private final Setting<Boolean> noPacketKick = register(new BooleanSetting("No Kick", true));
    private final Setting<Boolean> smooth = register(new BooleanSetting("Smooth", true));
    private final Setting<Boolean> extraPacket = register(new BooleanSetting("Extra Packets", true));

    private final List<PlayerMoveC2SPacket> packets = new ArrayList<>();
    private double serverX = 0;
    private double serverY = 0;
    private double serverZ = 0;
    private int tpId = 0;
    private float pitch = 0;
    private float yaw = 0;

    @Override
    public void onEnable() {
        pitch = MC.player.getPitch();
        yaw = MC.player.getYaw();

        serverX = MC.player.getX();
        serverY = MC.player.getY();
        serverZ = MC.player.getZ();
    }

    @Override
    public void onDisable() {
        tpId = 0;
        packets.clear();
    }

    private double getFallSpeed() {
        return mode.getValue() == Mode.SETBACK ? 0.003 : 0.03;
    }

    @EventHandler
    public EventListener<MovePlayerEvent> movePlayerEvent = new EventListener<>(event -> {
        boolean keyPressed = MC.options.keyForward.isPressed() ||
                MC.options.keyBack.isPressed() ||
                MC.options.keyLeft.isPressed() ||
                MC.options.keyRight.isPressed();

        // calculate movement velocities
        double x = MC.player.getX();
        double y = MC.player.getY() + (MC.options.keyJump.isPressed() ? upSpeed.getValue() : (!keyPressed ? 0 : -getFallSpeed()));
        double z = MC.player.getZ();

        if(keyPressed) {
            float yaw = MC.player.getYaw();
            float forward = 1;

            if(MC.player.forwardSpeed < 0) {
                yaw += 180;
                forward = -0.5f;
            } else if(MC.player.forwardSpeed > 0) forward = 0.5f;

            if(MC.player.sidewaysSpeed > 0) yaw -= 90 * forward;
            if(MC.player.sidewaysSpeed < 0) yaw += 90 * forward;

            yaw = (float) Math.toRadians(yaw);
            x += -Math.sin(yaw) * speed.getValue();
            z += Math.cos(yaw) * speed.getValue();
        }

        // calculate rotation
        float yaw = smooth.getValue() ? this.yaw : MC.player.getYaw();
        float pitch = smooth.getValue() ? this.pitch : MC.player.getPitch();

        // send move packets and keep player and the position the server thinks its at
        if(mode.getValue() == Mode.SETBACK) setbackMove(x, y, z, yaw, pitch);
        else fastMove(x, y, z, yaw, pitch);
        MC.player.setPos(serverX, serverY, serverZ);
        MC.player.setVelocity(0, 0, 0);
        event.set(0, 0, 0);
    });

    private void fastMove(double x, double y, double z, float yaw, float pitch) {
        // send new position and confirm with predicted id
        MC.player.networkHandler.sendPacket(add(new PlayerMoveC2SPacket.Full(x, y, z, yaw, pitch, MC.player.isOnGround())));
        MC.player.networkHandler.sendPacket(new TeleportConfirmC2SPacket(++tpId));

        // send out of bounds packet and confirm with predicted id
        MC.player.networkHandler.sendPacket(add(new PlayerMoveC2SPacket.Full(MC.player.getX(), getBounds(), MC.player.getZ(), yaw, pitch, MC.player.isOnGround())));
        MC.player.networkHandler.sendPacket(new TeleportConfirmC2SPacket(++tpId));

        // send new position and confirm again
        MC.player.networkHandler.sendPacket(add(new PlayerMoveC2SPacket.Full(x, y, z, yaw, pitch, MC.player.isOnGround())));
        if(extraPacket.getValue()) MC.player.networkHandler.sendPacket(new TeleportConfirmC2SPacket(tpId - 1));
        MC.player.networkHandler.sendPacket(new TeleportConfirmC2SPacket(tpId));
        if(extraPacket.getValue()) MC.player.networkHandler.sendPacket(new TeleportConfirmC2SPacket(tpId + 1));
    }

    private void setbackMove(double x, double y, double z, float yaw, float pitch) {
        // send out of bounds packet and confirm with predicted id
        MC.player.networkHandler.sendPacket(add(new PlayerMoveC2SPacket.Full(MC.player.getX(), getBounds(), MC.player.getZ(), yaw, pitch, MC.player.isOnGround())));
        MC.player.networkHandler.sendPacket(new TeleportConfirmC2SPacket(++tpId));

        // send new position and confirm again
        MC.player.networkHandler.sendPacket(add(new PlayerMoveC2SPacket.Full(x, y, z, yaw, pitch, MC.player.isOnGround())));
        if(extraPacket.getValue()) MC.player.networkHandler.sendPacket(new TeleportConfirmC2SPacket(tpId - 1));
        MC.player.networkHandler.sendPacket(new TeleportConfirmC2SPacket(tpId));
        if(extraPacket.getValue()) MC.player.networkHandler.sendPacket(new TeleportConfirmC2SPacket(tpId + 1));
    }

    private double getBounds() {
        return (bounds.getValue() == Bounds.DOWN ? MC.player.getY() - 1850 : (bounds.getValue() == Bounds.UP ? MC.player.getY() + 1850 : getGround()));
    }

    private double getGround() {
        BlockPos pos = MC.player.getBlockPos();
        while(MC.world.getBlockState((pos = pos.down())).getBlock() == Blocks.AIR) {  }
        return pos.getY();
    }

    @EventHandler
    public EventListener<PacketEvent.Sent> onPacketSent = new EventListener<>(event -> {
        if(event.getPacket() instanceof PlayerMoveC2SPacket) {
            if(packets.contains(event.getPacket())) packets.remove(event.getPacket());
            else event.setCancelled(true);

            if(!event.isCancelled() && smooth.getValue()) {
                ((PlayerMoveC2SPacketAccessor) event.getPacket()).setPitch(pitch);
                ((PlayerMoveC2SPacketAccessor) event.getPacket()).setYaw(yaw);
            }
        }
    });

    @EventHandler
    public EventListener<PacketEvent.Receive> onPacketReceive = new EventListener<>(event -> {
        if(MC.player != null && event.getPacket() instanceof PlayerPositionLookS2CPacket) {
            PlayerPositionLookS2CPacket packet = (PlayerPositionLookS2CPacket) event.getPacket();

            serverX = packet.getX();
            serverY = packet.getY();
            serverZ = packet.getZ();
            tpId = packet.getTeleportId();

            if(smooth.getValue()) {
                ((PlayerPositionLookS2CPacketAccessor) event.getPacket()).setPitch(MC.player.getPitch());
                ((PlayerPositionLookS2CPacketAccessor) event.getPacket()).setYaw(MC.player.getYaw());
            }
        }

        if(noPacketKick.getValue() && event.getPacket() instanceof CloseHandledScreenC2SPacket) event.setCancelled(true);
    });

    private <T extends PlayerMoveC2SPacket> T add(T packet) {
        packets.add(packet);
        return packet;
    }
}
