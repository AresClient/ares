package dev.tigr.ares.forge.impl.modules.movement;

import dev.tigr.ares.core.feature.module.Category;
import dev.tigr.ares.core.feature.module.Module;
import dev.tigr.ares.core.setting.Setting;
import dev.tigr.ares.core.setting.settings.BooleanSetting;
import dev.tigr.ares.core.setting.settings.EnumSetting;
import dev.tigr.ares.core.setting.settings.StringSetting;
import dev.tigr.ares.core.setting.settings.numerical.DoubleSetting;
import dev.tigr.ares.core.util.global.ReflectionHelper;
import dev.tigr.ares.forge.event.events.movement.ElytraMoveEvent;
import dev.tigr.ares.forge.event.events.player.PacketEvent;
import dev.tigr.simpleevents.listener.EventHandler;
import dev.tigr.simpleevents.listener.EventListener;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.network.play.client.CPacketEntityAction;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.event.entity.living.LivingEvent;
import org.lwjgl.input.Keyboard;

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
    @EventHandler
    public EventListener<LivingEvent.LivingUpdateEvent> livingUpdateEvent = new EventListener<>(event -> {
        if(!MC.player.isElytraFlying()) {
            if(ezTakeOff.getValue() && !MC.player.onGround && MC.gameSettings.keyBindJump.isKeyDown())
                MC.player.connection.sendPacket(new CPacketEntityAction(MC.player, CPacketEntityAction.Action.START_FALL_FLYING));

            return;
        }

        if(mode.getValue() == FlightMode.BOOST) {
            if(MC.player.capabilities.isFlying) {
                MC.player.capabilities.isFlying = false;
            }

            if(MC.player.isInWater()) {
                MC.player.connection.sendPacket(new CPacketEntityAction(MC.player, CPacketEntityAction.Action.START_FALL_FLYING));
            }

            float yaw = (float) Math.toRadians(MC.player.rotationYaw);

            if(MC.gameSettings.keyBindForward.isKeyDown()) {
                MC.player.motionX -= MathHelper.sin(yaw) * speed.getValue() / 2;
                MC.player.motionZ += MathHelper.cos(yaw) * speed.getValue() / 2;
            } else if(MC.gameSettings.keyBindBack.isKeyDown()) {
                MC.player.motionX += MathHelper.sin(yaw) * speed.getValue() / 2;
                MC.player.motionZ -= MathHelper.cos(yaw) * speed.getValue() / 2;
            }
        }

        if(mode.getValue() == FlightMode.FLIGHT || mode.getValue() == FlightMode.PACKET) {
            MC.player.capabilities.isFlying = true;
            MC.player.jumpMovementFactor = speed.getValue().floatValue();
            MC.player.capabilities.setFlySpeed(speed.getValue().floatValue() / 8);

            // packet mode stabilization
            if(mode.getValue() == FlightMode.PACKET && !Keyboard.isKeyDown(MC.gameSettings.keyBindJump.getKeyCode()) && !Keyboard.isKeyDown(MC.gameSettings.keyBindSneak.getKeyCode())) {
                ReflectionHelper.setPrivateValue(KeyBinding.class, MC.gameSettings.keyBindJump, true, "pressed", "field_74513_e");
                ReflectionHelper.setPrivateValue(KeyBinding.class, MC.gameSettings.keyBindSneak, true, "pressed", "field_74513_e");
            }

            if(Keyboard.isKeyDown(MC.gameSettings.keyBindJump.getKeyCode()))
                MC.player.motionY += speed.getValue();
            if(Keyboard.isKeyDown(MC.gameSettings.keyBindSneak.getKeyCode()))
                MC.player.motionY -= speed.getValue();
        }
    });
    @EventHandler
    public EventListener<ElytraMoveEvent> elytraMoveEvent = new EventListener<>(event -> {
        if(mode.getValue() == FlightMode.CONTROL && MC.player != null && MC.player.ticksExisted > 20 && MC.world != null) {
            event.y = 0;

            if(!MC.gameSettings.keyBindForward.isKeyDown() &&
                    !MC.gameSettings.keyBindBack.isKeyDown() &&
                    !MC.gameSettings.keyBindLeft.isKeyDown() &&
                    !MC.gameSettings.keyBindRight.isKeyDown()) event.x = event.z = 0;
            else {
                float yaw = MC.player.rotationYaw;
                float forward = 1;

                if(MC.player.moveForward < 0) {
                    yaw += 180;
                    forward = -0.5f;
                } else if(MC.player.moveForward > 0) forward = 0.5f;

                if(MC.player.moveStrafing > 0) yaw -= 90 * forward;
                if(MC.player.moveStrafing < 0) yaw += 90 * forward;

                yaw = (float) Math.toRadians(yaw);

                event.x = -Math.sin(yaw) * speed.getValue();
                event.z = Math.cos(yaw) * speed.getValue();

                boolean fall = true;

                if(MC.gameSettings.keyBindSneak.isKeyDown()) event.y = -speed.getValue();
                else if(MC.gameSettings.keyBindJump.isKeyDown() && MC.player.moveForward > 0) {
                    event.y += speed.getValue();

                    fall = false;
                }

                if(fall) {
                    if(!fallSpeed.getValue().equals("0.000100000002")) {
                        try {
                            float speed = Float.parseFloat(fallSpeed.getValue());
                            MC.player.posY -= speed;
                        } catch(Exception ignored) {
                        }

                    } else MC.player.posY -= 0.000100000002f;
                }
            }
        }

        if(mode.getValue() == FlightMode.PACKET && MC.player != null && MC.player.ticksExisted > 20 && MC.world != null) {
            event.y = 0;
            MC.player.fallDistance = 0;
        }
    });
    @EventHandler
    public EventListener<PacketEvent.Sent> packetSentEvent = new EventListener<>(event -> {
        if(event.getPacket() instanceof CPacketPlayer) {
            if(spoofPitch.getValue() && MC.player.isElytraFlying() && mode.getValue() == FlightMode.CONTROL)
                ReflectionHelper.setPrivateValue(CPacketPlayer.class, (CPacketPlayer) event.getPacket(), 0, "pitch", "field_149473_f");
            if(mode.getValue() == FlightMode.PACKET)
                MC.player.connection.sendPacket(new CPacketEntityAction(MC.player, CPacketEntityAction.Action.START_FALL_FLYING));
        }
    });

    @Override
    public void onEnable() {
        if(mode.getValue() == FlightMode.FLIGHT || mode.getValue() == FlightMode.PACKET) MC.player.posY += 0.02;
    }

    @Override
    public void onDisable() {
        if(mode.getValue() == FlightMode.FLIGHT || mode.getValue() == FlightMode.PACKET) {
            MC.player.capabilities.isFlying = false;
            MC.player.capabilities.allowFlying = false;
            MC.player.connection.sendPacket(new CPacketEntityAction(MC.player, CPacketEntityAction.Action.START_FALL_FLYING));

            if(mode.getValue() == FlightMode.PACKET) {
                ReflectionHelper.setPrivateValue(KeyBinding.class, MC.gameSettings.keyBindJump, Keyboard.isKeyDown(MC.gameSettings.keyBindJump.getKeyCode()), "pressed", "field_74513_e");
                ReflectionHelper.setPrivateValue(KeyBinding.class, MC.gameSettings.keyBindSneak, Keyboard.isKeyDown(MC.gameSettings.keyBindSneak.getKeyCode()), "pressed", "field_74513_e");
            }
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
