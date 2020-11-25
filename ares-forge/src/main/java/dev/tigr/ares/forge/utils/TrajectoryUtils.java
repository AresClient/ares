package dev.tigr.ares.forge.utils;

import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.*;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Tigermouthbear 10/4/20
 */
public class TrajectoryUtils {
    public static Result calculate(EntityLivingBase thrower, Projectiles projectile) {
        List<Vec3d> points = new ArrayList<>();

        SimulatedThrowable simulatedThrowable = new SimulatedThrowable(thrower, projectile);
        for(int i = 0; i < 100; i++) {
            if(simulatedThrowable.collided) break;
            simulatedThrowable.update();
            points.add(simulatedThrowable.getPos());
        }

        return new Result(points);
    }

    public static Projectiles getProjectile(EntityLivingBase entityLivingBase) {
        if(entityLivingBase.getHeldItemMainhand().isEmpty()) return null;

        Item item = entityLivingBase.getHeldItemMainhand().getItem();
        if(item instanceof ItemBow && entityLivingBase.isHandActive()) return Projectiles.BOW;
        else if(item instanceof ItemExpBottle) return Projectiles.XP_BOTTLE;
        else if(item instanceof ItemSnowball) return Projectiles.SNOWBALL;
        else if(item instanceof ItemEnderPearl) return Projectiles.ENDER_PEARL;
        else if(item instanceof ItemEgg) return Projectiles.EGG;
        else if(item instanceof ItemSplashPotion) return Projectiles.POTION;

        return null;
    }

    public enum Projectiles {
        BOW(0, 1.0f, 0.05000000074505806f),
        EGG(0, 1.5f, 0.03f),
        ENDER_PEARL(0, 1.5f, 0.03f),
        SNOWBALL(0, 1.5f, 0.03f),
        XP_BOTTLE(-20, 0.7f, 0.07f),
        POTION(-20, 0.5f, 0.05f);

        private final float pitchOffset;
        private final float velocity;
        private final float gravity;

        Projectiles(float pitchOffset, float velocity, float gravity) {
            this.pitchOffset = pitchOffset;
            this.velocity = velocity;
            this.gravity = gravity;
        }
    }

    public static class Result {
        private final List<Vec3d> points;
        private final Vec3d hitVec;

        private Result(List<Vec3d> points) {
            this.points = points;
            this.hitVec = points.get(points.size() - 1);
        }

        public List<Vec3d> getPoints() {
            return points;
        }

        public Vec3d getHitVec() {
            return hitVec;
        }
    }

    private static class SimulatedThrowable {
        private final EntityLivingBase entityThrower;
        private final Projectiles projectile;

        private AxisAlignedBB boundingBox;

        private double x;
        private double y;
        private double z;

        private double motionX;
        private double motionY;
        private double motionZ;

        private float rotationYaw;
        private float rotationPitch;

        private boolean collided = false;

        public SimulatedThrowable(EntityLivingBase entityThrower, Projectiles projectile) {
            this.entityThrower = entityThrower;
            this.projectile = projectile;

            // set position of throwable
            Vec3d pos = entityThrower.getPositionVector();
            x = pos.x;
            y = pos.y + (double)entityThrower.getEyeHeight() - 0.10000000149011612D;
            z = pos.z;

            // offset projectile so its not inside player
            x -= MathHelper.cos(entityThrower.rotationYaw / 180.0F * (float) Math.PI) * 0.16F;
            z -= MathHelper.sin(entityThrower.rotationYaw / 180.0F * (float) Math.PI) * 0.16F;

            // create bounding box
            updateBoundingBox();

            // get heading
            double f = (double) -MathHelper.sin(entityThrower.rotationYaw * 0.017453292F) * MathHelper.cos(entityThrower.rotationPitch * 0.017453292F);
            double f1 = (double) -MathHelper.sin((entityThrower.rotationPitch + projectile.pitchOffset) * 0.017453292F);
            double f2 = (double) MathHelper.cos(entityThrower.rotationYaw * 0.017453292F) * MathHelper.cos(entityThrower.rotationPitch * 0.017453292F);

            // set velocity
            double divisor = MathHelper.sqrt(f * f + f1 * f1 + f2 * f2);
            double velocity = getInitialVelocity();
            f = f / divisor;
            f1 = f1 / divisor;
            f2 = f2 / divisor;
            f = f * velocity;
            f1 = f1 * velocity;
            f2 = f2 * velocity;
            motionX = f;
            motionY = f1;
            motionZ = f2;

            float rotation = MathHelper.sqrt(f * f + f2 * f2);
            this.rotationYaw = (float)(MathHelper.atan2(f, f2) * (180D / Math.PI));
            this.rotationPitch = (float)(MathHelper.atan2(f1, (double)rotation) * (180D / Math.PI));

            motionX += entityThrower.motionX;
            motionZ += entityThrower.motionZ;
            if(!entityThrower.onGround) motionY += entityThrower.motionY;
        }

        private void update() {
            // predict next pos
            Vec3d currPos = new Vec3d(x, y, z);
            Vec3d nextPos = currPos.add(motionX, motionY, motionZ);

            // anti void
            if(currPos.y <= 0) {
                collided = true;
                return;
            }

            // check if next pos hits block
            RayTraceResult collision = entityThrower.getEntityWorld().rayTraceBlocks(currPos, nextPos, false, true, false);

            // check if next pos hits entity
            List<Entity> collidedEntities = entityThrower.world.getEntitiesWithinAABBExcludingEntity(entityThrower, boundingBox.expand(motionX, motionY, motionZ).expand(1, 1, 1));
            Entity collidedEntity = null;
            double greatestDistance = 0;
            for(Entity entity: collidedEntities) {
                if(entity.canBeCollidedWith() && entity != entityThrower) {

                    float size = entity.getCollisionBorderSize();
                    AxisAlignedBB entityHitbox = entity.getEntityBoundingBox().expand(size, size, size);
                    RayTraceResult intercept = entityHitbox.calculateIntercept(currPos, nextPos);

                    if(intercept != null) {
                        double distance = currPos.distanceTo(intercept.hitVec);
                        if(collidedEntity == null || distance < greatestDistance) {
                            collidedEntity = entity;
                            collision = intercept;
                            greatestDistance = distance;
                        }
                    }
                }
            }

            if(collision != null) {
                updatePosition(collision.hitVec);
                collided = true;
                return;
            }

            // add velocity
            x += this.motionX;
            y += this.motionY;
            z += this.motionZ;

            // add air or water drag
            float drag = 0.99F;
            if(entityThrower.getEntityWorld().isMaterialInBB(boundingBox, Material.WATER)) drag = projectile == Projectiles.BOW ? 0.6F : 0.8F;
            motionX *= drag;
            motionY *= drag;
            motionZ *= drag;

            // add gravity
            motionY -= projectile.gravity;

            // update bounding box
            updateBoundingBox();
        }

        private void updatePosition(Vec3d vec) {
            x = vec.x;
            y = vec.y;
            z = vec.z;
        }

        // updates bounding box to new position
        private void updateBoundingBox() {
            double size = (projectile == Projectiles.BOW ? 0.5d : 0.25d) / 2.0d;

            this.boundingBox = new AxisAlignedBB(
                    x - size, y - size, z - size,
                    x + size, y + size, z + size
            );
        }

        private float getInitialVelocity() {
            if(projectile == Projectiles.BOW) {
                Item heldItem = entityThrower.getHeldItemMainhand().getItem();
                if(heldItem instanceof ItemBow) {
                    float velocity = (float) (heldItem.getMaxItemUseDuration(entityThrower.getHeldItemMainhand()) - entityThrower.getItemInUseCount()) / 20.0F;
                    velocity = (velocity * velocity + velocity * 2.0F) / 3.0F;
                    return ((Math.min(velocity, 1.0f)) * 2.0f) * 1.5f;
                } else return 1.0f;
            } else return projectile.velocity;
        }

        public Vec3d getPos() {
            return new Vec3d(x, y, z);
        }

        public void setPos(double x, double y, double z) {
            this.x = x;
            this.y = y;
            this.z = z;
        }
    }
}
