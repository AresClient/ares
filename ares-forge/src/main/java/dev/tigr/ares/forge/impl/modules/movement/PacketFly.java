package dev.tigr.ares.forge.impl.modules.movement;

import dev.tigr.ares.core.feature.module.Category;
import dev.tigr.ares.core.feature.module.Module;
import dev.tigr.ares.core.setting.Setting;
import dev.tigr.ares.core.setting.settings.BooleanSetting;
import dev.tigr.ares.core.setting.settings.EnumSetting;
import dev.tigr.ares.core.setting.settings.numerical.DoubleSetting;
import dev.tigr.ares.core.util.global.ReflectionHelper;
import dev.tigr.ares.forge.event.events.client.NetworkExceptionEvent;
import dev.tigr.ares.forge.event.events.movement.MovePlayerEvent;
import dev.tigr.ares.forge.event.events.player.PacketEvent;
import dev.tigr.simpleevents.listener.EventHandler;
import dev.tigr.simpleevents.listener.EventListener;
import net.minecraft.init.Blocks;
import net.minecraft.network.play.client.CPacketConfirmTeleport;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.network.play.server.SPacketCloseWindow;
import net.minecraft.network.play.server.SPacketPlayerPosLook;
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

    private final List<CPacketPlayer> packets = new ArrayList<>();
    private double serverX = 0;
    private double serverY = 0;
    private double serverZ = 0;
    private int tpId = 0;
    private float pitch = 0;
    private float yaw = 0;

    @Override
    public void onEnable() {
        pitch = MC.player.rotationPitch;
        yaw = MC.player.rotationYaw;

        serverX = MC.player.posX;
        serverY = MC.player.posY;
        serverZ = MC.player.posZ;
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
        boolean keyPressed = MC.gameSettings.keyBindForward.isKeyDown() ||
                MC.gameSettings.keyBindBack.isKeyDown() ||
                MC.gameSettings.keyBindLeft.isKeyDown() ||
                MC.gameSettings.keyBindRight.isKeyDown();

        // calculate movement velocities
        double x = MC.player.posX;
        double y = MC.player.posY + (MC.gameSettings.keyBindJump.isKeyDown() ? upSpeed.getValue() : (!keyPressed ? 0 : -getFallSpeed()));
        double z = MC.player.posZ;

        if(keyPressed) {
            float yaw = MC.player.rotationYaw;
            float forward = 1;

            if(MC.player.moveForward < 0) {
                yaw += 180;
                forward = -0.5f;
            } else if(MC.player.moveForward > 0) forward = 0.5f;

            if(MC.player.moveStrafing > 0) yaw -= 90 * forward;
            if(MC.player.moveStrafing < 0) yaw += 90 * forward;

            yaw = (float) Math.toRadians(yaw);
            x += -Math.sin(yaw) * speed.getValue();
            z += Math.cos(yaw) * speed.getValue();
        }

        // calculate rotation
        float yaw = smooth.getValue() ? this.yaw : MC.player.rotationYaw;
        float pitch = smooth.getValue() ? this.pitch : MC.player.rotationPitch;

        // send move packets and keep player and the position the server thinks its at
        if(mode.getValue() == Mode.SETBACK) setbackMove(x, y, z, yaw, pitch);
        else fastMove(x, y, z, yaw, pitch);
        MC.player.setPosition(serverX, serverY, serverZ);
        MC.player.setVelocity(0, 0, 0);
        event.set(0, 0, 0);
    });

    private void fastMove(double x, double y, double z, float yaw, float pitch) {
        // send new position and confirm with predicted id
        MC.player.connection.sendPacket(add(new CPacketPlayer.PositionRotation(x, y, z, yaw, pitch, MC.player.onGround)));
        MC.player.connection.sendPacket(new CPacketConfirmTeleport(++tpId));

        // send out of bounds packet and confirm with predicted id
        MC.player.connection.sendPacket(add(new CPacketPlayer.PositionRotation(MC.player.posX, getBounds(), MC.player.posZ, yaw, pitch, MC.player.onGround)));
        MC.player.connection.sendPacket(new CPacketConfirmTeleport(++tpId));

        // send new position and confirm again
        MC.player.connection.sendPacket(add(new CPacketPlayer.PositionRotation(x, y, z, yaw, pitch, MC.player.onGround)));
        if(extraPacket.getValue()) MC.player.connection.sendPacket(new CPacketConfirmTeleport(tpId - 1));
        MC.player.connection.sendPacket(new CPacketConfirmTeleport(tpId));
        if(extraPacket.getValue()) MC.player.connection.sendPacket(new CPacketConfirmTeleport(tpId + 1));
    }

    private void setbackMove(double x, double y, double z, float yaw, float pitch) {
        // send out of bounds packet and confirm with predicted id
        MC.player.connection.sendPacket(add(new CPacketPlayer.PositionRotation(MC.player.posX, getBounds(), MC.player.posZ, yaw, pitch, MC.player.onGround)));
        MC.player.connection.sendPacket(new CPacketConfirmTeleport(++tpId));

        // send new position and confirm again
        MC.player.connection.sendPacket(add(new CPacketPlayer.PositionRotation(x, y, z, yaw, pitch, MC.player.onGround)));
        if(extraPacket.getValue()) MC.player.connection.sendPacket(new CPacketConfirmTeleport(tpId - 1));
        MC.player.connection.sendPacket(new CPacketConfirmTeleport(tpId));
        if(extraPacket.getValue()) MC.player.connection.sendPacket(new CPacketConfirmTeleport(tpId + 1));
    }

    private double getBounds() {
        return (bounds.getValue() == Bounds.DOWN ? MC.player.posY - 1850 : (bounds.getValue() == Bounds.UP ? MC.player.posY + 1850 : getGround()));
    }

    private double getGround() {
        BlockPos pos = MC.player.getPosition();
        while(MC.world.getBlockState((pos = pos.down())).getBlock() == Blocks.AIR) {  }
        return pos.getY();
    }

    @EventHandler
    public EventListener<PacketEvent.Sent> onPacketSent = new EventListener<>(event -> {
        if(event.getPacket() instanceof CPacketPlayer) {
            if(packets.contains((CPacketPlayer) event.getPacket())) packets.remove((CPacketPlayer) event.getPacket());
            else event.setCancelled(true);

            if(!event.isCancelled() && smooth.getValue()) {
                ReflectionHelper.setPrivateValue(CPacketPlayer.class, (CPacketPlayer) event.getPacket(), pitch, "pitch", "field_149473_f");
                ReflectionHelper.setPrivateValue(CPacketPlayer.class, (CPacketPlayer) event.getPacket(), yaw, "yaw", "field_149476_e");
            }
        }
    });

    @EventHandler
    public EventListener<PacketEvent.Receive> onPacketReceive = new EventListener<>(event -> {
        if(MC.player != null && event.getPacket() instanceof SPacketPlayerPosLook) {
            SPacketPlayerPosLook packet = (SPacketPlayerPosLook) event.getPacket();

            serverX = packet.getX();
            serverY = packet.getY();
            serverZ = packet.getZ();
            tpId = packet.getTeleportId();

            if(smooth.getValue()) {
                ReflectionHelper.setPrivateValue(SPacketPlayerPosLook.class, (SPacketPlayerPosLook) event.getPacket(), MC.player.rotationPitch, "pitch", "field_148937_e");
                ReflectionHelper.setPrivateValue(SPacketPlayerPosLook.class, (SPacketPlayerPosLook) event.getPacket(), MC.player.rotationYaw, "yaw", "field_148936_d");
            }
        }

        if(noPacketKick.getValue() && event.getPacket() instanceof SPacketCloseWindow) event.setCancelled(true);
    });

    @EventHandler
    public EventListener<NetworkExceptionEvent> networkExceptionEvent = new EventListener<>(event -> {
        if(noPacketKick.getValue()) event.setCancelled(true);
    });

    private <T extends CPacketPlayer> T add(T packet) {
        packets.add(packet);
        return packet;
    }
}
