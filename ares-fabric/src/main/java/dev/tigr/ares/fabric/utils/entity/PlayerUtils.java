package dev.tigr.ares.fabric.utils.entity;

import dev.tigr.ares.Wrapper;
import dev.tigr.ares.core.feature.FriendManager;
import dev.tigr.ares.fabric.impl.modules.player.Freecam;
import dev.tigr.ares.fabric.utils.MathUtils;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Vec3d;

/**
 * Split from WorldUtils 10/17/21 - Makrennel
 */
public class PlayerUtils implements Wrapper {
    /** Positional Getters */

    public static double getEyeY(PlayerEntity player) {
        return player.getY() + player.getEyeHeight(player.getPose());
    }

    public static Vec3d getEyePos(PlayerEntity player) {
        return new Vec3d(player.getX(), getEyeY(player), player.getZ());
    }


    /** Calculation */

    public static double[] calculateLookFromPlayer(double x, double y, double z, PlayerEntity player) {
        return MathUtils.calculateAngle(new Vec3d(player.getX(), getEyeY(player), player.getZ()), new Vec3d(x,y,z));
    }


    /** Checks */

    public static boolean isValidTarget(PlayerEntity player, double distance) {
        return !friendCheck(player)
                && !player.isRemoved()
                && !hasZeroHealth(player)
                && isPlayerInRange(player, distance)
                && player != MC.player
                && player != Freecam.INSTANCE.clone;
    }

    public static boolean isPlayerInRange(PlayerEntity player, double distance) {
        return MathUtils.isInRange(SelfUtils.getPlayer().getPos(), player.getPos(), distance);
    }

    public static boolean hasZeroHealth(PlayerEntity player) {
        return player.getHealth() <= 0;
    }

    public static boolean friendCheck(PlayerEntity player) {
        return FriendManager.isFriend((player).getGameProfile().getName());
    }
}
