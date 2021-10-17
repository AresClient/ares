package dev.tigr.ares.forge.utils.entity;

import dev.tigr.ares.Wrapper;
import dev.tigr.ares.core.feature.FriendManager;
import dev.tigr.ares.forge.utils.MathUtils;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.Vec3d;

/**
 * Split from WorldUtils 10/17/21 - Makrennel
 */
public class PlayerUtils implements Wrapper {
    /** Positional Getters */

    public static double getEyeY(EntityPlayer player) {
        return player.posY + player.getEyeHeight();
    }

    public static Vec3d getEyePos(EntityPlayer player) {
        return new Vec3d(player.posX, getEyeY(player), player.posZ);
    }


    /** Calculation */

    public static double[] calculateLookFromPlayer(double x, double y, double z, EntityPlayer player) {
        return MathUtils.calculateAngle(new Vec3d(player.posX, getEyeY(player), player.posZ), new Vec3d(x,y,z));
    }


    /** Checks */

    public static boolean isValidTarget(EntityPlayer player) {
        return isValidTarget(player, -1, false);
    }

    public static boolean isValidTarget(EntityPlayer player, double distance) {
        return isValidTarget(player, distance, true);
    }

    public static boolean isValidTarget(EntityPlayer player, double distance, boolean doDistance) {
        return !FriendManager.isFriend(player.getGameProfile().getName())
                && !player.isDead
                && !(player.getHealth() <= 0)
                && !shouldDistance(player, distance, doDistance)
                && player != MC.player;
    }

    private static boolean shouldDistance(EntityPlayer entity, double distance, boolean doDistance) {
        if(doDistance) return MC.player.getDistanceSq(entity) > (distance * distance);
        else return false;
    }
}
