package dev.tigr.ares.forge.utils;

import dev.tigr.ares.Wrapper;
import net.minecraft.block.state.IBlockState;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.potion.Potion;
import net.minecraft.util.CombatRules;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.Explosion;

/**
 * Split from WorldUtils 10/17/21 - Makrennel
 */
public class MathUtils implements Wrapper {
    public static double[] calculateAngle(Vec3d a, Vec3d b) {
        double
                x = a.x - b.x,
                y = a.y - b.y,
                z = a.z - b.z,
                d = Math.sqrt(x * x + y * y + z * z),
                pitch = Math.toDegrees(Math.asin(y / d)),
                yaw = Math.toDegrees(Math.atan2(z / d, x / d) + Math.PI / 2);

        return new double[] {
                yaw,
                pitch
        };
    }
    
    public static double[] getMovement(final double speed, float forwards, float sideways, float yawDegrees) {
        return getMovement(speed, forwards, sideways, Math.toRadians(yawDegrees));
    }

    public static double[] getMovement(final double speed, float forwards, float sideways, double yaw) {
        if(forwards != 0) {
            if(sideways > 0) yaw += forwards > 0 ? - Math.PI / 4 : Math.PI / 4;
            else if(sideways < 0) yaw += forwards > 0 ? Math.PI / 4 : - Math.PI / 4;

            sideways = 0;

            if(forwards > 0) forwards = 1;
            else if(forwards < 0) forwards = -1;
        }

        yaw += Math.PI / 2;

        return new double[] {
                forwards * speed * Math.cos(yaw) + sideways * speed * Math.sin(yaw),
                forwards * speed * Math.sin(yaw) - sideways * speed * Math.cos(yaw)
        };
    }
    
    public enum DmgCalcMode {
        DAMAGE,
        DISTANCE
    }

    // Calculate score based on the mode of calculation
    public static double getScore(Vec3d pos, EntityPlayer player, DmgCalcMode dmgCalcMode, boolean predictMovement) {
        double score;
        if(dmgCalcMode == DmgCalcMode.DISTANCE) {
            score = Math.abs(player.posY - pos.y) + Math.abs(player.posX - pos.x) + Math.abs(player.posZ - pos.z);

            if(MathUtils.rayTrace(pos, player.getPositionVector()) == RayTraceResult.Type.BLOCK) score = -1;
        } else {
            score = 200 - MathUtils.getDamage(pos, player, predictMovement);
        }

        return score;
    }

    //Ideally we want beds to place on the upper hitbox of the player on 1.15+ to force the player into crawl position
    public static double getDistanceScoreBed(Vec3d pos, Entity player) {
        double score = Math.abs(player.posY + 1 - pos.y) + Math.abs(player.posX - pos.x) + Math.abs(player.posZ - pos.z);

        if(MathUtils.rayTrace(pos, new Vec3d(player.posX, player.posY +1, player.posZ)) == RayTraceResult.Type.BLOCK) score = -1;

        return score;
    }

    // damage calculations
    public static float getDamage(Vec3d pos, EntityPlayer entity, boolean predictMovement) {
        double blockDensity;
        if(predictMovement) blockDensity = entity.world.getBlockDensity(pos, entity.getEntityBoundingBox().offset(entity.motionX, entity.motionY, entity.motionZ));
        else blockDensity = entity.world.getBlockDensity(pos, entity.getEntityBoundingBox());
        
        double power = (1.0D - (entity.getDistance(pos.x, pos.y, pos.z) / 12.0D)) * blockDensity;
        float damage = (float) ((int) ((power * power + power) / 2.0D * 7.0D * 12.0D + 1.0D));

        // world difficulty damage change
        int difficulty = MC.world.getDifficulty().getId();
        damage *= (difficulty == 0 ? 0 : (difficulty == 2 ? 1 : (difficulty == 1 ? 0.5f : 1.5f)));

        return getReduction(entity, damage, new Explosion(MC.world, null, pos.x, pos.y, pos.z, 6F, false, true));
    }

    // get blast reduction off armor and potions
    public static float getReduction(EntityPlayer player, float damage, Explosion explosion) {
        // armor
        damage = CombatRules.getDamageAfterAbsorb(damage, (float) player.getTotalArmorValue(), (float) player.getEntityAttribute(SharedMonsterAttributes.ARMOR_TOUGHNESS).getAttributeValue());

        // enchantment
        damage *= (1.0F - (float) EnchantmentHelper.getEnchantmentModifierDamage(player.getArmorInventoryList(), DamageSource.causeExplosionDamage(explosion)) / 25.0F);

        // potions
        if(player.isPotionActive(Potion.getPotionById(11))) damage -= damage / 4;

        return damage;
    }

    // raytracing
    public static RayTraceResult.Type rayTrace(Vec3d start, Vec3d end) {
        double minX = Math.min(start.x, end.x);
        double minY = Math.min(start.y, end.y);
        double minZ = Math.min(start.z, end.z);
        double maxX = Math.max(start.x, end.x);
        double maxY = Math.max(start.y, end.y);
        double maxZ = Math.max(start.z, end.z);

        for(double x = minX; x > maxX; x += 1) {
            for(double y = minY; y > maxY; y += 1) {
                for(double z = minZ; z > maxZ; z += 1) {
                    IBlockState blockState = MC.world.getBlockState(new BlockPos(x, y, z));

                    if(blockState.getBlock() == Blocks.OBSIDIAN
                            || blockState.getBlock() == Blocks.BEDROCK
                            || blockState.getBlock() == Blocks.BARRIER)
                        return RayTraceResult.Type.BLOCK;
                }
            }
        }

        return RayTraceResult.Type.MISS;
    }
}
