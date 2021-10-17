package dev.tigr.ares.fabric.utils.entity;

import dev.tigr.ares.Wrapper;
import dev.tigr.ares.core.feature.FriendManager;
import dev.tigr.ares.fabric.utils.MathUtils;
import net.minecraft.client.network.OtherClientPlayerEntity;
import net.minecraft.entity.Entity;
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

    public static boolean isValidTarget(Entity entity) {
        return isValidTarget(entity, -1, false);
    }

    public static boolean isValidTarget(Entity entity, double distance) {
        return isValidTarget(entity, distance, true);
    }

    public static boolean isValidTarget(Entity entity, double distance, boolean doDistance) {
        return (entity instanceof PlayerEntity || entity instanceof OtherClientPlayerEntity)
                && !friendCheck(entity)
                && !entity.isRemoved()
                && !hasZeroHealth(entity)
                && !shouldDistance(entity, distance, doDistance)
                && entity != MC.player;
    }

    private static boolean shouldDistance(Entity entity, double distance, boolean doDistance) {
        if(doDistance) return MC.player.distanceTo(entity) > distance;
        else return false;
    }

    public static boolean hasZeroHealth(PlayerEntity playerEntity) {
        return hasZeroHealth((Entity) playerEntity);
    }

    public static boolean hasZeroHealth(Entity entity) {
        if(entity instanceof PlayerEntity) {
            return (((PlayerEntity) entity).getHealth() <= 0);
        } else return false;
    }

    public static boolean friendCheck(PlayerEntity playerEntity) {
        return friendCheck((Entity) playerEntity);
    }

    public static boolean friendCheck(Entity entity) {
        if(entity instanceof PlayerEntity) {
            return FriendManager.isFriend(((PlayerEntity) entity).getGameProfile().getName());
        } else return false;
    }
}
