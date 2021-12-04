package dev.tigr.ares.fabric.utils;

import dev.tigr.ares.Wrapper;
import dev.tigr.ares.fabric.utils.entity.EntityUtils;
import dev.tigr.ares.fabric.utils.render.RenderUtils;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.DamageUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.stat.Stats;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.Difficulty;
import net.minecraft.world.RaycastContext;

import java.util.Objects;

/**
 * Split from WorldUtils 10/17/21 - Makrennel
 */
public class MathUtils implements Wrapper {
    public static double squaredDistanceBetween(double x1, double y1, double z1, double x2, double y2, double z2) {
        double
                x = x1 - x2,
                y = y1 - y2,
                z = z1 - z2;

        return x * x + y * y + z * z;
    }

    public static double squaredDistanceBetween(Vec3d pos1, Vec3d pos2) {
        double
                x = pos1.x - pos2.x,
                y = pos1.y - pos2.y,
                z = pos1.z - pos2.z;

        return x * x + y * y + z * z;
    }

    public static Vec3d getClosestPointOfBlockPos(Vec3d pos, BlockPos blockPos) {
        return getClosestPointOfBox(pos, MC.world.getBlockState(blockPos).isAir() ? new Box(blockPos) : Objects.requireNonNull(RenderUtils.getBoundingBox(blockPos)));
    }

    public static Vec3d getClosestPointOfBox(Vec3d pos, Box box) {
        double[]
                a = new double[] {pos.x, pos.y, pos.z},
                b = new double[] {box.maxX, box.maxY, box.maxZ},
                c = new double[] {box.minX, box.minY, box.minZ};

        for(int i = 0; i <= 2; i++) {
            if(a[i] > b[i]) a[i] = b[i];
            if(a[i] < c[i]) a[i] = c[i];
        }

        return new Vec3d(a[0], a[1], a[2]);
    }

    public static boolean isInRange(Vec3d pos, Vec3d pos2, double range) {
        return squaredDistanceBetween(pos, pos2) <= range * range;
    }

    public static boolean isInRangeClosestPoint(Vec3d pos, Box box, double range) {
        return squaredDistanceBetween(pos, getClosestPointOfBox(pos, box)) <= range * range;
    }

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
    public static double getScore(Vec3d pos, PlayerEntity player, DmgCalcMode dmgCalcMode, boolean predictMovement) {
        double score;
        if(dmgCalcMode == DmgCalcMode.DISTANCE) {
            score = Math.abs(player.getPos().y - pos.y) + Math.abs(player.getPos().x - pos.x) + Math.abs(player.getPos().z - pos.z);

            if(MathUtils.rayTrace(pos, player.getPos()) == HitResult.Type.BLOCK) score = -1;
        } else {
            score = 200 - MathUtils.getDamage(pos, player, predictMovement);
        }

        return score;
    }

    //Ideally we want beds to place on the upper hitbox of the player on 1.15+ to force the player into crawl position
    public static double getDistanceScoreBed(Vec3d pos, PlayerEntity player) {
        double score =
                Math.abs(
                        (MC.world.getBlockState(EntityUtils.getBlockPosCorrected(player).up()).getMaterial().isReplaceable() ? player.getPos().y + 1
                                : (MC.world.getBlockState(EntityUtils.getBlockPosCorrected(player).up(2)).getMaterial().isReplaceable() ? player.getPos().y + 2
                                : player.getPos().y)) - pos.y)
                + Math.abs(player.getPos().x - pos.x) + Math.abs(player.getPos().z - pos.z);

        if(MathUtils.rayTrace(pos, new Vec3d(player.getX(), player.getY() +1, player.getZ())) == HitResult.Type.BLOCK) score = -1;

        return score;
    }

    // damage calculations
    public static float getDamage(Vec3d vec3d, PlayerEntity player, boolean predictMovement) {
        float f2 = 12.0f;
        double d7 = Math.sqrt(player.squaredDistanceTo(vec3d)) / f2;
        if(d7 <= 1.0D) {
            double d8 = player.getX() - vec3d.x;
            double d9 = player.getEyeY() - vec3d.y;
            double d10 = player.getZ() - vec3d.z;
            double d11 = Math.sqrt(d8 * d8 + d9 * d9 + d10 * d10);
            if(d11 != 0.0D) {
                double d12 = getExposure(vec3d, player, predictMovement);
                double d13 = (1.0D - d7) * d12;
                float damage = transformForDifficulty((float)((int)((d13 * d13 + d13) / 2.0D * 7.0D * (double)f2 + 1.0D)));
                damage = DamageUtil.getDamageLeft(damage, (float) player.getArmor(), (float) player.getAttributeValue(EntityAttributes.GENERIC_ARMOR_TOUGHNESS));
                damage = getReduction(player, damage, DamageSource.GENERIC);
                return damage;
            }
        }
        return 0.0f;
    }

    public static float transformForDifficulty(float f) {
        if(MC.world.getDifficulty() == Difficulty.PEACEFUL) f = 0.0F;
        if(MC.world.getDifficulty() == Difficulty.EASY) f = Math.min(f / 2.0F + 1.0F, f);
        if(MC.world.getDifficulty() == Difficulty.HARD) f = f * 3.0F / 2.0F;
        return f;
    }

    // Explosion.getExposure
    public static float getExposure(Vec3d source, Entity entity, boolean predictMovement) {
        Box box = entity.getBoundingBox();
        if(predictMovement) box.offset(entity.getVelocity().x, entity.getVelocity().y, entity.getVelocity().z);

        double d = 1.0D / ((box.maxX - box.minX) * 2.0D + 1.0D);
        double e = 1.0D / ((box.maxY - box.minY) * 2.0D + 1.0D);
        double f = 1.0D / ((box.maxZ - box.minZ) * 2.0D + 1.0D);
        double g = (1.0D - Math.floor(1.0D / d) * d) / 2.0D;
        double h = (1.0D - Math.floor(1.0D / f) * f) / 2.0D;
        if (!(d < 0.0D) && !(e < 0.0D) && !(f < 0.0D)) {
            int i = 0;
            int j = 0;

            for(float k = 0.0F; k <= 1.0F; k = (float)((double)k + d)) {
                for(float l = 0.0F; l <= 1.0F; l = (float)((double)l + e)) {
                    for(float m = 0.0F; m <= 1.0F; m = (float)((double)m + f)) {
                        double n = MathHelper.lerp(k, box.minX, box.maxX);
                        double o = MathHelper.lerp(l, box.minY, box.maxY);
                        double p = MathHelper.lerp(m, box.minZ, box.maxZ);
                        Vec3d vec3d = new Vec3d(n + g, o, p + h);
                        if (entity.world.raycast(new RaycastContext(vec3d, source, RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, entity)).getType() == HitResult.Type.MISS) {
                            ++i;
                        }

                        ++j;
                    }
                }
            }

            return (float)i / (float)j;
        } else {
            return 0.0F;
        }
    }

    // get blast reduction off armor and potions
    public static float getReduction(PlayerEntity player, float f, DamageSource damageSource) {
        if (player.hasStatusEffect(StatusEffects.RESISTANCE) && damageSource != DamageSource.OUT_OF_WORLD) {
            int i = (player.getStatusEffect(StatusEffects.RESISTANCE).getAmplifier() + 1) * 5;
            int j = 25 - i;
            float f1 = f * (float)j;
            float f2 = f;
            f = Math.max(f1 / 25.0F, 0.0F);
            float f3 = f2 - f;
            if (f3 > 0.0F && f3 < 3.4028235E37F) {
                if (player instanceof ServerPlayerEntity) {
                    player.increaseStat(Stats.DAMAGE_RESISTED, Math.round(f3 * 10.0F));
                } else if (damageSource.getAttacker() instanceof ServerPlayerEntity) {
                    ((ServerPlayerEntity)damageSource.getAttacker()).increaseStat(Stats.DAMAGE_DEALT_RESISTED, Math.round(f3 * 10.0F));
                }
            }
        }

        if (f <= 0.0F) {
            return 0.0F;
        } else {
            int k = EnchantmentHelper.getProtectionAmount(player.getArmorItems(), damageSource);
            if (k > 0) {
                f = DamageUtil.getInflictedDamage(f, (float)k);
            }

            return f;
        }
    }

    // raytracing
    public static HitResult.Type rayTrace(Vec3d start, Vec3d end) {
        double minX = Math.min(start.x, end.x);
        double minY = Math.min(start.y, end.y);
        double minZ = Math.min(start.z, end.z);
        double maxX = Math.max(start.x, end.x);
        double maxY = Math.max(start.y, end.y);
        double maxZ = Math.max(start.z, end.z);

        for(double x = minX; x > maxX; x += 1) {
            for(double y = minY; y > maxY; y += 1) {
                for(double z = minZ; z > maxZ; z += 1) {
                    BlockState blockState = MC.world.getBlockState(new BlockPos(x, y, z));

                    if(blockState.getBlock() == Blocks.OBSIDIAN
                            || blockState.getBlock() == Blocks.BEDROCK
                            || blockState.getBlock() == Blocks.BARRIER)
                        return HitResult.Type.BLOCK;
                }
            }
        }

        return HitResult.Type.MISS;
    }
}
