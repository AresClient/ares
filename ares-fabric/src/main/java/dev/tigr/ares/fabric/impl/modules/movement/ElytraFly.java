package dev.tigr.ares.fabric.impl.modules.movement;

import dev.tigr.ares.core.feature.module.Category;
import dev.tigr.ares.core.feature.module.Module;
import dev.tigr.ares.core.setting.Setting;
import dev.tigr.ares.core.setting.settings.BooleanSetting;
import dev.tigr.ares.core.setting.settings.EnumSetting;
import dev.tigr.ares.core.setting.settings.StringSetting;
import dev.tigr.ares.core.setting.settings.numerical.DoubleSetting;
import dev.tigr.ares.fabric.event.client.PacketEvent;
import dev.tigr.ares.fabric.event.movement.ElytraMoveEvent;
import dev.tigr.ares.fabric.mixin.accessors.PlayerMoveC2SPacketAccessor;
import dev.tigr.simpleevents.listener.EventHandler;
import dev.tigr.simpleevents.listener.EventListener;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.util.math.MathHelper;

/**
 * @author Tigermouthbear
 */
@Module.Info(name = "ElytraFly", description = "Rockets aren't needed", category = Category.MOVEMENT)
public class ElytraFly extends Module {
    private final Setting<FlightMode> mode = register(new EnumSetting<>("Mode", FlightMode.BOOST));
    private final Setting<Double> speed = register(new DoubleSetting("Speed", 0.9D, 0.01D, 4.0D));
    private final Setting<Boolean> ezTakeOff = register(new BooleanSetting("EzTakeoff", true));
    private final Setting<Boolean> spoofPitch = register(new BooleanSetting("Spoof Pitch", false));
    private final Setting<String> fallSpeed = register(new StringSetting("Fall Speed", "0.000100000002"));

    @Override
    public void onMotion() {
        if(!MC.player.isFallFlying()) {
            if(ezTakeOff.getValue() && !MC.player.isOnGround() && MC.options.keyJump.isPressed())
                MC.player.networkHandler.sendPacket(new ClientCommandC2SPacket(MC.player, ClientCommandC2SPacket.Mode.START_FALL_FLYING));

            return;
        }

        if(mode.getValue() == FlightMode.BOOST) {
            if(MC.player.getAbilities().flying) {
                MC.player.getAbilities().flying = false;
            }

            if(MC.player.isSubmergedInWater()) {
                MC.player.networkHandler.sendPacket(new ClientCommandC2SPacket(MC.player, ClientCommandC2SPacket.Mode.START_FALL_FLYING));
            }

            float yaw = (float) Math.toRadians(MC.player.getYaw());
            if(MC.options.keyForward.isPressed()) {
                MC.player.addVelocity(-MathHelper.sin(yaw) * speed.getValue() / 10, 0, MathHelper.cos(yaw) * speed.getValue() / 10);
            } else if(MC.options.keyBack.isPressed()) {
                MC.player.addVelocity(MathHelper.sin(yaw) * speed.getValue() / 10, 0, -MathHelper.cos(yaw) * speed.getValue() / 10);
            }
        }

        if(mode.getValue() == FlightMode.FLIGHT || mode.getValue() == FlightMode.PACKET) {
            MC.player.getAbilities().flying = true;
            MC.player.getAbilities().setFlySpeed(speed.getValue().floatValue() / 8);

            if(MC.options.keyJump.isPressed())
                MC.player.addVelocity(0, speed.getValue(), 0);
            if(MC.options.keyJump.isPressed())
                MC.player.addVelocity(0, -speed.getValue(), 0);
        }
    }

    @EventHandler
    public EventListener<ElytraMoveEvent> elytraMoveEvent = new EventListener<>(event -> {
        if(mode.getValue() == FlightMode.CONTROL && MC.player != null && TICKS > 20 && MC.world != null) {
            event.y = 0;

            if(!MC.options.keyForward.isPressed() &&
                    !MC.options.keyBack.isPressed() &&
                    !MC.options.keyLeft.isPressed() &&
                    !MC.options.keyRight.isPressed()) event.x = event.z = 0;
            else {
                float yaw = MC.player.getYaw();
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

                boolean fall = true;

                if(MC.options.keySneak.isPressed()) event.y = -speed.getValue();
                else if(MC.options.keyJump.isPressed() && MC.player.forwardSpeed > 0) {
                    event.y += speed.getValue();

                    fall = false;
                }

                if(fall) {
                    if(!fallSpeed.getValue().equals("0.000100000002")) {
                        try {
                            float speed = Float.parseFloat(fallSpeed.getValue());
                            MC.player.setPos(MC.player.getX(), MC.player.getY() - speed, MC.player.getZ());
                        } catch(Exception ignored) {
                        }

                    } else MC.player.setPos(MC.player.getX(), MC.player.getY() - 0.000100000002f, MC.player.getZ());
                }
            }
        }

        if(mode.getValue() == FlightMode.PACKET && MC.player != null && TICKS> 20 && MC.world != null) {
            event.y = 0;
            MC.player.fallDistance = 0;
        }
    });

    @EventHandler
    public EventListener<PacketEvent.Sent> packetSentEvent = new EventListener<>(event -> {
        if(event.getPacket() instanceof PlayerMoveC2SPacket) {
            if(spoofPitch.getValue() && MC.player.isFallFlying() && mode.getValue() == FlightMode.CONTROL)
                ((PlayerMoveC2SPacketAccessor) event.getPacket()).setPitch(0);
            if(mode.getValue() == FlightMode.PACKET)
                MC.player.networkHandler.sendPacket(new ClientCommandC2SPacket(MC.player, ClientCommandC2SPacket.Mode.START_FALL_FLYING));
        }
    });

    @Override
    public void onEnable() {
        if(mode.getValue() == FlightMode.FLIGHT || mode.getValue() == FlightMode.PACKET) MC.player.setPos(MC.player.getX(), MC.player.getY() + 0.02, MC.player.getZ());
    }

    @Override
    public void onDisable() {
        if(mode.getValue() == FlightMode.FLIGHT || mode.getValue() == FlightMode.PACKET) {
            MC.player.getAbilities().flying = false;
            MC.player.getAbilities().allowFlying = false;
            MC.player.networkHandler.sendPacket(new ClientCommandC2SPacket(MC.player, ClientCommandC2SPacket.Mode.START_FALL_FLYING));
        }
    }

    @Override
    public String getInfo() {
        return mode.getValue().name();
    }

    enum FlightMode {
        BOOST,
        CONTROL,
        FLIGHT,
        PACKET
    }
}
