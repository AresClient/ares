package dev.tigr.ares.fabric.impl.modules.movement;

import dev.tigr.ares.core.feature.module.Category;
import dev.tigr.ares.core.feature.module.Module;
import dev.tigr.ares.core.setting.Setting;
import dev.tigr.ares.core.setting.settings.BooleanSetting;
import dev.tigr.ares.core.setting.settings.EnumSetting;
import dev.tigr.ares.fabric.utils.entity.SelfUtils;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;

/**
 * @author  Doogie13
 * @since 2021/09/28
 */
@Module.Info(name = "PacketFly", description = "Fly using packets", category = Category.MOVEMENT)
public class PacketFly extends Module {

    private final Setting<bound> bounds = register(new EnumSetting<>("Bounds", bound.ALTERNATE));
    private final Setting<Boolean> stable = register(new BooleanSetting("Stablilise", false));

    int tmr;

    @Override
    public void onDisable() {
        MC.player.abilities.allowFlying = false;
        MC.player.abilities.setFlySpeed(0.05f);
    }

    @Override
    public void onTick() {
        tmr++;
    }

    @Override
    public void onMotion() {
        MC.player.setVelocity(0, 0, 0);

        if (stable.getValue()) {

            MC.player.abilities.allowFlying = true;
            MC.player.abilities.setFlySpeed(0);

        }

        double x = MC.player.getX();
        double y = MC.player.getY();
        double z = MC.player.getZ();

        if (MC.player.verticalCollision && !MC.player.isOnGround()) // we are in contact with ceiling and not floor
            MC.player.setSneaking(tmr % 2 == 0);

        if (MC.options.keySneak.isPressed() && !MC.options.keyJump.isPressed()) {

            y -= 0.0624;

        }
        if (MC.options.keyJump.isPressed()) {

            y += 0.0624;

        }
        if ((MC.options.keyForward.isPressed() || MC.options.keyBack.isPressed() || MC.options.keyLeft.isPressed() || MC.options.keyRight.isPressed()) && (!MC.player.isOnGround() || MC.player.verticalCollision)) {
            double[] dir = SelfUtils.getMovement(0.0624);

            x += dir[0];
            z += dir[1];

        }

        if (tmr % 2 == 0) {

            MC.player.setPos(MC.player.getX(),MC.player.getY() + 0.01, MC.player.getZ());

        } else {

            MC.player.setPos(MC.player.getX(),MC.player.getY() -0.01, MC.player.getZ());

        }

        if (!MC.player.isOnGround() || MC.player.horizontalCollision){
            MC.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.PositionOnly(x, y, z, false));

            switch (bounds.getValue()) {
                case UP:
                    MC.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.PositionOnly(MC.player.getX(), MC.player.getY() + 69420, MC.player.getZ(),false));
                case DOWN:
                    MC.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.PositionOnly(MC.player.getX(), MC.player.getY() - 69420, MC.player.getZ(), false));
                case ALTERNATE: {
                    if (tmr % 2 == 0)
                        MC.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.PositionOnly(MC.player.getX(), MC.player.getY() + 69420, MC.player.getZ(), false));
                    else
                        MC.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.PositionOnly(MC.player.getX(), MC.player.getY() - 69420, MC.player.getZ(), false));
                }

            }
        }
    }

    enum bound {
        UP, DOWN, ALTERNATE, NONE
    }
}
