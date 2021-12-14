package dev.tigr.ares.fabric.utils.entity;

import dev.tigr.ares.Wrapper;
import dev.tigr.ares.core.feature.FriendManager;
import dev.tigr.ares.fabric.utils.WorldUtils;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.entity.mob.AmbientEntity;
import net.minecraft.entity.mob.EndermanEntity;
import net.minecraft.entity.mob.PiglinEntity;
import net.minecraft.entity.passive.IronGolemEntity;
import net.minecraft.entity.passive.PassiveEntity;
import net.minecraft.entity.passive.SquidEntity;
import net.minecraft.entity.passive.WolfEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

/**
 * Split from WorldUtils 10/17/21 - Makrennel
 */
public class EntityUtils implements Wrapper {
    /** Positional Getters */

    public static BlockPos getBlockPosCorrected(Entity entity) {
        return WorldUtils.roundBlockPos(
                new Vec3d(
                        Math.floor(entity.getX()),
                        entity.getY(),
                        Math.floor(entity.getZ())
                )
        );
    }


    /** Checks */

    public static boolean isPassive(Entity entity) {
        if(entity instanceof IronGolemEntity && ((IronGolemEntity) entity).getAngryAt() == null) return true;
        else if(entity instanceof WolfEntity && (!((WolfEntity) entity).isAttacking() || ((WolfEntity) entity).getOwner() == MC.player)) return true;
        else if(entity instanceof EndermanEntity) return !((EndermanEntity) entity).isAngry();
        else return entity instanceof AmbientEntity || entity instanceof PassiveEntity || entity instanceof SquidEntity;
    }

    public static boolean isHostile(Entity entity) {
        if(entity instanceof IronGolemEntity) return ((IronGolemEntity) entity).getAngryAt() == MC.player.getUuid() && ((IronGolemEntity) entity).getAngryAt() != null;
        else if(entity instanceof WolfEntity) return ((WolfEntity) entity).isAttacking() && ((WolfEntity) entity).getOwner() != MC.player;
        else if(entity instanceof PiglinEntity) return ((PiglinEntity) entity).isAngryAt(MC.player);
        else if(entity instanceof EndermanEntity) return ((EndermanEntity) entity).isAngry();
        return entity.getType().getSpawnGroup() == SpawnGroup.MONSTER;
    }

    public static boolean isBot(Entity entity) {
        return entity instanceof PlayerEntity && entity.isInvisibleTo(MC.player) && !entity.isOnGround() && !entity.collides();
    }

    public static boolean isTarget(Entity entity, boolean players, boolean friends, boolean teammates, boolean passive, boolean hostile, boolean nametagged, boolean bots) {
        if(!(entity instanceof LivingEntity) || entity == MC.player) return false;

        if(players && entity instanceof PlayerEntity) {
            if(FriendManager.isFriend(((PlayerEntity) entity).getGameProfile().getName())) return friends;
            if(entity.getScoreboardTeam() == MC.player.getScoreboardTeam() && MC.player.getScoreboardTeam() != null) return teammates;
            return true;
        }
        if(!nametagged && entity.hasCustomName()) return false;
        if(passive && isPassive(entity)) return true;
        if(hostile && isHostile(entity)) return true;
        return bots && isBot(entity);
    }


    /** Actions */

    public static void moveEntityWithSpeed(Entity entity, double speed, boolean shouldMoveY) {
        float yaw = (float) Math.toRadians(MC.player.getYaw());

        double motionX = 0;
        double motionY = 0;
        double motionZ = 0;

        if(MC.player.input.pressingForward) {
            motionX = -(MathHelper.sin(yaw) * speed);
            motionZ = MathHelper.cos(yaw) * speed;
        } else if(MC.player.input.pressingBack) {
            motionX = MathHelper.sin(yaw) * speed;
            motionZ = -(MathHelper.cos(yaw) * speed);
        }

        if(MC.player.input.pressingLeft) {
            motionZ = MathHelper.sin(yaw) * speed;
            motionX = MathHelper.cos(yaw) * speed;
        } else if(MC.player.input.pressingRight) {
            motionZ = -(MathHelper.sin(yaw) * speed);
            motionX = -(MathHelper.cos(yaw) * speed);
        }

        if(shouldMoveY) {
            if(MC.player.input.jumping) {
                motionY = speed;
            } else if(MC.player.input.sneaking) {
                motionY = -speed;
            }
        }

        //strafe
        if(MC.player.input.pressingForward && MC.player.input.pressingLeft) {
            motionX = (MathHelper.cos(yaw) * speed) - (MathHelper.sin(yaw) * speed);
            motionZ = (MathHelper.cos(yaw) * speed) + (MathHelper.sin(yaw) * speed);
        } else if(MC.player.input.pressingLeft && MC.player.input.pressingBack) {
            motionX = (MathHelper.cos(yaw) * speed) + (MathHelper.sin(yaw) * speed);
            motionZ = -(MathHelper.cos(yaw) * speed) + (MathHelper.sin(yaw) * speed);
        } else if(MC.player.input.pressingBack && MC.player.input.pressingRight) {
            motionX = -(MathHelper.cos(yaw) * speed) + (MathHelper.sin(yaw) * speed);
            motionZ = -(MathHelper.cos(yaw) * speed) - (MathHelper.sin(yaw) * speed);
        } else if(MC.player.input.pressingRight && MC.player.input.pressingForward) {
            motionX = -(MathHelper.cos(yaw) * speed) - (MathHelper.sin(yaw) * speed);
            motionZ = (MathHelper.cos(yaw) * speed) - (MathHelper.sin(yaw) * speed);
        }

        entity.setVelocity(motionX, motionY, motionZ);
    }
}
