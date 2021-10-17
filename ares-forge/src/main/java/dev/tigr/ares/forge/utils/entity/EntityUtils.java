package dev.tigr.ares.forge.utils.entity;

import dev.tigr.ares.Wrapper;
import dev.tigr.ares.core.feature.FriendManager;
import dev.tigr.ares.forge.utils.WorldUtils;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityAgeable;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.entity.monster.EntityEnderman;
import net.minecraft.entity.monster.EntityIronGolem;
import net.minecraft.entity.monster.EntityPigZombie;
import net.minecraft.entity.passive.EntityAmbientCreature;
import net.minecraft.entity.passive.EntitySquid;
import net.minecraft.entity.passive.EntityWolf;
import net.minecraft.entity.player.EntityPlayer;
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
                        Math.floor(entity.posX),
                        entity.posY,
                        Math.floor(entity.posZ)
                )
        );
    }


    /** Checks */

    public static boolean isPassive(Entity entity) {
        if(entity instanceof EntityIronGolem && ((EntityIronGolem) entity).getRevengeTarget() == null) return true;
        else if(entity instanceof EntityWolf && (!((EntityWolf) entity).isAngry() || ((EntityWolf) entity).getOwner() == MC.player)) return true;
        else if(entity instanceof EntityEnderman) return !((EntityEnderman) entity).isScreaming();
        else return entity instanceof EntityAmbientCreature || entity instanceof EntityAgeable || entity instanceof EntitySquid;
    }

    public static boolean isHostile(Entity entity) {
        if(entity instanceof EntityIronGolem) return ((EntityIronGolem) entity).getRevengeTarget() == MC.player && ((EntityIronGolem) entity).getRevengeTarget() != null;
        else if(entity instanceof EntityWolf) return ((EntityWolf) entity).isAngry() && ((EntityWolf) entity).getOwner() != MC.player;
        else if(entity instanceof EntityPigZombie) return ((EntityPigZombie) entity).isAngry() || ((EntityPigZombie) entity).isArmsRaised();
        else if(entity instanceof EntityEnderman) return ((EntityEnderman) entity).isScreaming();
        return entity.isCreatureType(EnumCreatureType.MONSTER, false);
    }

    public static boolean isBot(Entity entity) {
        return entity instanceof EntityPlayer && entity.isInvisibleToPlayer(MC.player) && !entity.onGround && entity.isAirBorne && !entity.canBeCollidedWith();
    }

    public static boolean isTarget(Entity entity, boolean players, boolean friends, boolean teammates, boolean passive, boolean hostile, boolean nametagged, boolean bots) {
        if(!(entity instanceof EntityLivingBase) || entity == MC.player) return false;

        if(players && entity instanceof EntityPlayer) {
            if(FriendManager.isFriend(((EntityPlayer) entity).getGameProfile().getName())) return friends;
            if(entity.getTeam() == MC.player.getTeam() && MC.player.getTeam() != null) return teammates;
            return true;
        }
        if(!nametagged && entity.hasCustomName()) return false;
        if(passive && isPassive(entity)) return true;
        if(hostile && isHostile(entity)) return true;
        return bots && isBot(entity);
    }


    /** Actions */

    public static void moveEntityWithSpeed(Entity entity, double speed, boolean shouldMoveY) {
        float yaw = (float) Math.toRadians(MC.player.rotationYaw);

        if(MC.gameSettings.keyBindForward.isKeyDown()) {
            entity.motionX = -(MathHelper.sin(yaw) * speed);
            entity.motionZ = MathHelper.cos(yaw) * speed;
        } else if(MC.gameSettings.keyBindBack.isKeyDown()) {
            entity.motionX = MathHelper.sin(yaw) * speed;
            entity.motionZ = -(MathHelper.cos(yaw) * speed);
        }

        if(MC.gameSettings.keyBindLeft.isKeyDown()) {
            entity.motionZ = MathHelper.sin(yaw) * speed;
            entity.motionX = MathHelper.cos(yaw) * speed;
        } else if(MC.gameSettings.keyBindRight.isKeyDown()) {
            entity.motionZ = -(MathHelper.sin(yaw) * speed);
            entity.motionX = -(MathHelper.cos(yaw) * speed);
        }

        if(shouldMoveY) {
            if(MC.gameSettings.keyBindJump.isKeyDown()) {
                entity.motionY = speed;
            } else if(MC.gameSettings.keyBindSneak.isKeyDown()) {
                entity.motionY = -speed;
            }
        }

        //strafe
        if(MC.gameSettings.keyBindForward.isKeyDown() && MC.gameSettings.keyBindLeft.isKeyDown()) {
            entity.motionX = (MathHelper.cos(yaw) * speed) - (MathHelper.sin(yaw) * speed);
            entity.motionZ = (MathHelper.cos(yaw) * speed) + (MathHelper.sin(yaw) * speed);
        } else if(MC.gameSettings.keyBindLeft.isKeyDown() && MC.gameSettings.keyBindBack.isKeyDown()) {
            entity.motionX = (MathHelper.cos(yaw) * speed) + (MathHelper.sin(yaw) * speed);
            entity.motionZ = -(MathHelper.cos(yaw) * speed) + (MathHelper.sin(yaw) * speed);
        } else if(MC.gameSettings.keyBindBack.isKeyDown() && MC.gameSettings.keyBindRight.isKeyDown()) {
            entity.motionX = -(MathHelper.cos(yaw) * speed) + (MathHelper.sin(yaw) * speed);
            entity.motionZ = -(MathHelper.cos(yaw) * speed) - (MathHelper.sin(yaw) * speed);
        } else if(MC.gameSettings.keyBindRight.isKeyDown() && MC.gameSettings.keyBindForward.isKeyDown()) {
            entity.motionX = -(MathHelper.cos(yaw) * speed) - (MathHelper.sin(yaw) * speed);
            entity.motionZ = (MathHelper.cos(yaw) * speed) - (MathHelper.sin(yaw) * speed);
        }
    }
}
