package dev.tigr.ares.forge.impl.modules.movement;

import dev.tigr.ares.core.feature.module.Category;
import dev.tigr.ares.core.feature.module.Module;
import dev.tigr.ares.core.setting.Setting;
import dev.tigr.ares.core.setting.settings.BooleanSetting;
import dev.tigr.ares.core.setting.settings.EnumSetting;
import dev.tigr.ares.forge.utils.entity.SelfUtils;
import net.minecraft.network.play.client.CPacketPlayer;

/**
 * @author  Doogie13
 * @since 2021/09/28
 */
@Module.Info(name = "PacketFly", description = "Fly using packets", category = Category.MOVEMENT)
public class PacketFly extends Module {

    private final Setting<bound> bounds = register(new EnumSetting<>("Bounds", bound.ALTERNATE));
    private final Setting<Boolean> stable = register(new BooleanSetting("Stablilise", false));

    @Override
    public void onDisable() {
        MC.player.capabilities.isFlying = false;
        MC.player.capabilities.setFlySpeed(0.05f);
    }

    @Override
    public void onMotion() {
        MC.player.setVelocity(0, 0, 0);

        if (stable.getValue()) {

            MC.player.capabilities.isFlying = true;
            MC.player.capabilities.setFlySpeed(0);

        }

        double x = MC.player.posX;
        double y = MC.player.posY;
        double z = MC.player.posZ;

        if (MC.player.collidedVertically && !MC.player.onGround) // we are in contact with ceiling and not floor
            MC.player.setSneaking(MC.player.ticksExisted % 2 == 0);

        if (MC.gameSettings.keyBindSneak.isKeyDown() && !MC.gameSettings.keyBindJump.isKeyDown()) {

            y -= 0.0624;

        }
        if (MC.gameSettings.keyBindJump.isKeyDown()) {

            y += 0.0624;

        }
        if ((MC.gameSettings.keyBindForward.isKeyDown() || MC.gameSettings.keyBindBack.isKeyDown() || MC.gameSettings.keyBindLeft.isKeyDown() || MC.gameSettings.keyBindRight.isKeyDown()) && (!MC.player.onGround || MC.player.collidedVertically)) {
            double[] dir = SelfUtils.getMovement(0.0624);

            x += dir[0];
            z += dir[1];

        }

        if (MC.player.ticksExisted % 2 == 0) {

            MC.player.posY += 0.01;

        } else {

            MC.player.posY -= 0.01;
        }

        if (!MC.player.onGround || MC.player.collidedHorizontally){
            MC.player.connection.sendPacket(new CPacketPlayer.Position(x, y, z, false));

            switch (bounds.getValue()) {
                case UP:
                    MC.player.connection.sendPacket(new CPacketPlayer.PositionRotation(MC.player.posX, MC.player.posY + 69420, MC.player.posZ, MC.player.rotationYaw, MC.player.rotationPitch, false));
                case DOWN:
                    MC.player.connection.sendPacket(new CPacketPlayer.PositionRotation(MC.player.posX, MC.player.posY - 69420, MC.player.posZ, MC.player.rotationYaw, MC.player.rotationPitch, false));
                case ALTERNATE: {
                    if (MC.player.ticksExisted % 2 == 0)
                        MC.player.connection.sendPacket(new CPacketPlayer.PositionRotation(MC.player.posX, MC.player.posY + 69420, MC.player.posZ, MC.player.rotationYaw, MC.player.rotationPitch, false));
                    else
                        MC.player.connection.sendPacket(new CPacketPlayer.PositionRotation(MC.player.posX, MC.player.posY - 69420, MC.player.posZ, MC.player.rotationYaw, MC.player.rotationPitch, false));
                }

            }
        }
    }

    enum bound {
        UP, DOWN, ALTERNATE, NONE
    }
}
