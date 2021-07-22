package dev.tigr.ares.fabric.utils;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.ArrowEntity;
import net.minecraft.item.*;
import net.minecraft.tag.FluidTags;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * @author Tigermouthbear 10/4/20
 * ported to fabric on 1/4/21 by Tigermouthbear
 */
public class TrajectoryUtils {
    public static Result calculate(LivingEntity thrower, Projectiles projectile) {
        List<Vec3d> points = new ArrayList<>();

        SimulatedThrowable simulatedThrowable = new SimulatedThrowable(thrower, projectile);
        for(int i = 0; i < 100; i++) {
            if(simulatedThrowable.collided) break;
            simulatedThrowable.update();
            points.add(simulatedThrowable.getPos());
        }

        return new Result(simulatedThrowable.collideType, points);
    }

    public static Projectiles getProjectile(LivingEntity entityLivingBase) {
        if(entityLivingBase.getMainHandStack().isEmpty()) return null;

        Item item = entityLivingBase.getMainHandStack().getItem();
        if(item instanceof CrossbowItem || (item instanceof BowItem && entityLivingBase.isUsingItem())) return Projectiles.BOW;
        else if(item instanceof TridentItem) return Projectiles.TRIDENT;
        else if(item instanceof ExperienceBottleItem) return Projectiles.XP_BOTTLE;
        else if(item instanceof SnowballItem) return Projectiles.SNOWBALL;
        else if(item instanceof EnderPearlItem) return Projectiles.ENDER_PEARL;
        else if(item instanceof EggItem) return Projectiles.EGG;
        else if(item instanceof SplashPotionItem) return Projectiles.POTION;

        return null;
    }

    public enum Projectiles {
        BOW(0, 1.0f, 0.05000000074505806f),
        TRIDENT(0, 2.5f, 0.05000000074505806f),
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
        private final HitResult.Type type;
        private final List<Vec3d> points;
        private final Vec3d hitVec;

        private Result(HitResult.Type type, List<Vec3d> points) {
            this.type = type;
            this.points = points;
            this.hitVec = points.get(points.size() - 1);
        }

        public HitResult.Type getType() {
            return type;
        }

        public List<Vec3d> getPoints() {
            return points;
        }

        public Vec3d getHitVec() {
            return hitVec;
        }
    }

    private static class SimulatedThrowable {
        private final LivingEntity entityThrower;
        private final Projectiles projectile;
        private final Entity mock;

        private Box boundingBox;

        private double x;
        private double y;
        private double z;

        private double motionX;
        private double motionY;
        private double motionZ;

        private float yaw;
        private float pitch;

        private HitResult.Type collideType = HitResult.Type.MISS;
        private boolean collided = false;

        public SimulatedThrowable(LivingEntity entityThrower, Projectiles projectile) {
            this.entityThrower = entityThrower;
            this.projectile = projectile;

            // set position of throwable
            Vec3d pos = entityThrower.getPos();
            x = pos.x;
            y = pos.y + (double)entityThrower.getEyeHeight(entityThrower.getPose()) - 0.10000000149011612D;
            z = pos.z;

            // offset projectile so its not inside player
            x -= MathHelper.cos(entityThrower.yaw / 180.0F * (float) Math.PI) * 0.16F;
            z -= MathHelper.sin(entityThrower.yaw / 180.0F * (float) Math.PI) * 0.16F;

            // create bounding box
            updateBoundingBox();

            // get heading
            double f = (double) -MathHelper.sin(entityThrower.yaw * 0.017453292F) * MathHelper.cos(entityThrower.pitch * 0.017453292F);
            double f1 = (double) -MathHelper.sin((entityThrower.pitch + projectile.pitchOffset) * 0.017453292F);
            double f2 = (double) MathHelper.cos(entityThrower.yaw * 0.017453292F) * MathHelper.cos(entityThrower.pitch * 0.017453292F);

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
            this.yaw = (float)(MathHelper.atan2(f, f2) * (180D / Math.PI));
            this.pitch = (float)(MathHelper.atan2(f1, (double)rotation) * (180D / Math.PI));

            motionX += entityThrower.getVelocity().x;
            motionZ += entityThrower.getVelocity().z;
            if(!entityThrower.isOnGround()) motionY += entityThrower.getVelocity().y;

            mock = new ArrowEntity(entityThrower.world, x, y, z);
            mock.setBoundingBox(boundingBox);
            mock.setWorld(entityThrower.world);
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

            mock.setPos(x, y, z);
            mock.setBoundingBox(boundingBox);

            // check if next pos hits block
            HitResult collision = entityThrower.getEntityWorld().raycast(new RaycastContext(currPos, nextPos, RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, mock));

            // check if next pos hits entity
            List<Entity> collidedEntities = entityThrower.world.getOtherEntities(entityThrower, boundingBox.expand(motionX, motionY, motionZ).expand(1, 1, 1));
            Entity collidedEntity = null;
            double greatestDistance = 0;
            for(Entity entity: collidedEntities) {
                if(entity.collides() && entity != entityThrower) {
                    entity.calculateDimensions();
                    Box entityHitbox = entity.getBoundingBox();
                    Optional<Vec3d> intercept = entityHitbox.raycast(currPos, nextPos);

                    if(intercept.isPresent()) {
                        double distance = currPos.distanceTo(intercept.get());
                        if(collidedEntity == null || distance < greatestDistance) {
                            collidedEntity = entity;
                            collision = new EntityHitResult(entity, intercept.get());
                            greatestDistance = distance;
                        }
                    }
                }
            }

            if(collision != null && collision.getType() != HitResult.Type.MISS) {
                updatePosition(collision.getPos());
                collideType = collision.getType();
                collided = true;
                return;
            }

            // add velocity
            x += this.motionX;
            y += this.motionY;
            z += this.motionZ;

            // add air or water drag
            float drag = 0.99F;
            if(mock.updateMovementInFluid(FluidTags.WATER, 0.014D)) drag = projectile == Projectiles.BOW ? 0.6F : 0.8F;
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

            this.boundingBox = new Box(
                    x - size, y - size, z - size,
                    x + size, y + size, z + size
            );
        }

        private float getInitialVelocity() {
            if(projectile == Projectiles.BOW) {
                Item heldItem = entityThrower.getMainHandStack().getItem();
                if(heldItem instanceof BowItem) {
                    float velocity = (float) (heldItem.getMaxUseTime(entityThrower.getMainHandStack()) - entityThrower.getItemUseTimeLeft()) / 20.0F;
                    velocity = (velocity * velocity + velocity * 2.0F) / 3.0F;
                    return ((Math.min(velocity, 1.0f)) * 2.0f) * 1.5f;
                } else if(heldItem instanceof CrossbowItem){
                    return CrossbowItem.hasProjectile(entityThrower.getMainHandStack(), Items.FIREWORK_ROCKET) ? 1.6F : 3.15F;
                }
            }
            return projectile.velocity;
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
