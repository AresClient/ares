package dev.tigr.ares.forge.utils.entity;

import dev.tigr.ares.Wrapper;
import dev.tigr.ares.core.feature.FriendManager;
import dev.tigr.ares.forge.impl.modules.player.Freecam;
import dev.tigr.ares.forge.utils.MathUtils;
import dev.tigr.ares.forge.utils.WorldUtils;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
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

    public static boolean isValidTarget(EntityPlayer player, double distance) {
        return !friendCheck(player)
                && !player.isDead
                && !hasZeroHealth(player)
                && isPlayerInRange(player, distance)
                && player != MC.player
                && player != Freecam.INSTANCE.clone;
    }

    public static boolean isPlayerInRange(EntityPlayer player, double distance) {
        return MathUtils.isInRange(SelfUtils.getPlayer().getPositionVector(), player.getPositionVector(), distance);
    }

    public static boolean hasZeroHealth(EntityPlayer player) {
        return player.getHealth() <= 0;
    }

    public static boolean friendCheck(EntityPlayer player) {
        return FriendManager.isFriend((player).getGameProfile().getName());
    }

    public static boolean isInBurrow(EntityPlayer entityPlayer){
        BlockPos pos = new BlockPos(WorldUtils.getMiddlePosition(entityPlayer.posX), entityPlayer.posY, WorldUtils.getMiddlePosition(entityPlayer.posZ));
        BlockPos playerPos = WorldUtils.roundBlockPos(new Vec3d(pos.getX(), entityPlayer.posY, pos.getZ()));

        return WorldUtils.isBlastProofBlock(playerPos);
    }
}
