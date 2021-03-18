package dev.tigr.ares.fabric.utils;

import dev.tigr.ares.Wrapper;
import dev.tigr.ares.core.feature.FriendManager;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.ShapeContext;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.entity.mob.AmbientEntity;
import net.minecraft.entity.mob.EndermanEntity;
import net.minecraft.entity.mob.PiglinEntity;
import net.minecraft.entity.passive.IronGolemEntity;
import net.minecraft.entity.passive.PassiveEntity;
import net.minecraft.entity.passive.SquidEntity;
import net.minecraft.entity.passive.WolfEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * @author Tigermouthbear 9/26/20
 */
public class WorldUtils implements Wrapper {
    public static boolean placeBlockMainHand(BlockPos pos) {
        return placeBlockMainHand(pos, true, false);
    }

    public static boolean placeBlockMainHand(BlockPos pos, Boolean rotate) {
        return placeBlockMainHand(pos, rotate, false);
    }

    public static boolean placeBlockMainHand(BlockPos pos, Boolean rotate, Boolean ignoreEntity) {
        return placeBlock(Hand.MAIN_HAND, pos, rotate, ignoreEntity);
    }

    public static boolean placeBlockNoRotate(Hand hand, BlockPos pos) {
        return placeBlock(hand, pos, false, false);
    }

    public static boolean placeBlock(Hand hand, BlockPos pos) {
        placeBlock(hand, pos, true, false);
        return true;
    }

    public static boolean placeBlock(Hand hand, BlockPos pos, Boolean rotate) {
        placeBlock(hand, pos, rotate, false);
        return true;
    }

    public static boolean placeBlock(Hand hand, BlockPos pos, Boolean rotate, Boolean ignoreEntity) {
        // make sure place is empty if ignoreEntity is not true
        if (ignoreEntity) {
            if (!MC.world.getBlockState(pos).getMaterial().isReplaceable())
                return false;
        } else if (!MC.world.getBlockState(pos).getMaterial().isReplaceable() || !MC.world.canPlace(Blocks.OBSIDIAN.getDefaultState(), pos, ShapeContext.absent()))
            return false;

        Vec3d eyesPos = new Vec3d(MC.player.getX(),
                MC.player.getY() + MC.player.getEyeHeight(MC.player.getPose()),
                MC.player.getZ());

        Vec3d hitVec = null;
        BlockPos neighbor = null;
        Direction side2 = null;
        for(Direction side: Direction.values()) {
            neighbor = pos.offset(side);
            side2 = side.getOpposite();

            // check if neighbor can be right clicked aka it isnt air
            if(MC.world.getBlockState(neighbor).isAir()) {
                neighbor = null;
                side2 = null;
                continue;
            }

            hitVec = new Vec3d(neighbor.getX(), neighbor.getY(), neighbor.getZ()).add(0.5, 0.5, 0.5).add(new Vec3d(side2.getUnitVector()).multiply(0.5));
            break;
        }

        // Air place if no neighbour was found
        if(hitVec == null) hitVec = new Vec3d(pos.getX(), pos.getY(), pos.getZ());
        if(neighbor == null) neighbor = pos;
        if(side2 == null) side2 = Direction.UP;

        // place block
        double diffX = hitVec.x - eyesPos.x;
        double diffY = hitVec.y - eyesPos.y;
        double diffZ = hitVec.z - eyesPos.z;

        double diffXZ = Math.sqrt(diffX * diffX + diffZ * diffZ);

        float yaw = (float) Math.toDegrees(Math.atan2(diffZ, diffX)) - 90F;
        float pitch = (float) -Math.toDegrees(Math.atan2(diffY, diffXZ));

        float[] rotations = {
                MC.player.yaw
                        + MathHelper.wrapDegrees(yaw - MC.player.yaw),
                MC.player.pitch + MathHelper
                        .wrapDegrees(pitch - MC.player.pitch)};

        if (rotate) {
            MC.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.LookOnly(rotations[0], rotations[1], MC.player.isOnGround()));
        }
        MC.player.networkHandler.sendPacket(new ClientCommandC2SPacket(MC.player, ClientCommandC2SPacket.Mode.PRESS_SHIFT_KEY));
        MC.interactionManager.interactBlock(MC.player, MC.world, hand, new BlockHitResult(hitVec, side2, neighbor, false));
        MC.player.swingHand(hand);
        MC.player.networkHandler.sendPacket(new ClientCommandC2SPacket(MC.player, ClientCommandC2SPacket.Mode.RELEASE_SHIFT_KEY));

        return true;
    }

    public static final List<Block> NONSOLID_BLOCKS = Arrays.asList(
            Blocks.AIR, Blocks.LAVA, Blocks.WATER, Blocks.GRASS,
            Blocks.VINE, Blocks.SEAGRASS, Blocks.TALL_SEAGRASS,
            Blocks.SNOW, Blocks.TALL_GRASS, Blocks.FIRE, Blocks.VOID_AIR);

    public static boolean canReplace(BlockPos pos) {
        return NONSOLID_BLOCKS.contains(MC.world.getBlockState(pos).getBlock()) && MC.world.getOtherEntities(null, new Box(pos)).stream().noneMatch(Entity::collides);
    }

    public static void moveEntityWithSpeed(Entity entity, double speed, boolean shouldMoveY) {
        float yaw = (float) Math.toRadians(MC.player.yaw);

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

    public static HoleType isHole(BlockPos pos) {
        BlockState[] blockStates = new BlockState[]{
                MC.world.getBlockState(pos),
                MC.world.getBlockState(pos.add(0, 1, 0)),
                MC.world.getBlockState(pos.add(0, 2, 0)),
                MC.world.getBlockState(pos.add(0, -1, 0)),
                MC.world.getBlockState(pos.add(1, 0, 0)),
                MC.world.getBlockState(pos.add(0, 0, 1)),
                MC.world.getBlockState(pos.add(-1, 0, 0)),
                MC.world.getBlockState(pos.add(0, 0, -1))
        };

        boolean isBedrock =
                (!blockStates[0].getMaterial().blocksMovement())
                        &&
                        (!blockStates[1].getMaterial().blocksMovement())
                        &&
                        (!blockStates[2].getMaterial().blocksMovement())
                        &&
                        (blockStates[3].getBlock().equals(Blocks.BEDROCK))
                        &&
                        (blockStates[4].getBlock().equals(Blocks.BEDROCK))
                        &&
                        (blockStates[5].getBlock().equals(Blocks.BEDROCK))
                        &&
                        (blockStates[6].getBlock().equals(Blocks.BEDROCK))
                        &&
                        (blockStates[7].getBlock().equals(Blocks.BEDROCK));

        if(isBedrock) return HoleType.BEDROCK;

        boolean bedrockOrObby =
                (!blockStates[0].getMaterial().blocksMovement())
                        &&
                        (!blockStates[1].getMaterial().blocksMovement())
                        &&
                        (!blockStates[2].getMaterial().blocksMovement())
                        &&
                        (blockStates[3].getBlock().equals(Blocks.BEDROCK) || blockStates[3].getBlock().equals(Blocks.OBSIDIAN))
                        &&
                        (blockStates[4].getBlock().equals(Blocks.BEDROCK) || blockStates[4].getBlock().equals(Blocks.OBSIDIAN))
                        &&
                        (blockStates[5].getBlock().equals(Blocks.BEDROCK) || blockStates[5].getBlock().equals(Blocks.OBSIDIAN))
                        &&
                        (blockStates[6].getBlock().equals(Blocks.BEDROCK) || blockStates[6].getBlock().equals(Blocks.OBSIDIAN))
                        &&
                        (blockStates[7].getBlock().equals(Blocks.BEDROCK) || blockStates[7].getBlock().equals(Blocks.OBSIDIAN));

        if(bedrockOrObby) return HoleType.OBBY;

        boolean isSolid =
                (!blockStates[0].getMaterial().blocksMovement())
                        &&
                        (!blockStates[1].getMaterial().blocksMovement())
                        &&
                        (!blockStates[2].getMaterial().blocksMovement())
                        &&
                        (blockStates[3].getMaterial().isSolid())
                        &&
                        (blockStates[4].getMaterial().isSolid())
                        &&
                        (blockStates[5].getMaterial().isSolid())
                        &&
                        (blockStates[6].getMaterial().isSolid())
                        &&
                        (blockStates[7].getMaterial().isSolid());

        if(isSolid) return HoleType.OTHER;

        return HoleType.NONE;
    }

    public static List<BlockPos> getAllInBox(final int x1, final int y1, final int z1, final int x2, final int y2, final int z2) {
        List<BlockPos> list = new ArrayList<>();
        // wanted to see how inline I could make this XD, good luck any future readers
        for(int x = Math.min(x1, x2); x <= Math.max(x1, x2); x++) for(int y = Math.min(y1, y2); y <= Math.max(y1, y2); y++) for(int z = Math.min(z1, z2); z <= Math.max(z1, z2); z++) list.add(new BlockPos(x, y, z));
        return list;
    }

    public static List<BlockPos> getBlocksInReachDistance() {
        List<BlockPos> cube = new ArrayList<>();
        for(int x = -4; x <= 4; x++)
            for(int y = -4; y <= 4; y++)
                for(int z = -4; z <= 4; z++)
                    cube.add(MC.player.getBlockPos().add(x, y, z));

        return cube.stream().filter(pos -> MC.player.squaredDistanceTo(new Vec3d(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ())) <= 18.0625).collect(Collectors.toList());
    }

    //Credit to KAMI for code below
    public static double[] calculateLookAt(double px, double py, double pz, PlayerEntity me) {
        double dirx = me.getX() - px;
        double diry = me.getY() + me.getEyeHeight(me.getPose()) - py;
        double dirz = me.getZ() - pz;

        double len = Math.sqrt(dirx * dirx + diry * diry + dirz * dirz);

        dirx /= len;
        diry /= len;
        dirz /= len;

        double pitch = Math.asin(diry);
        double yaw = Math.atan2(dirz, dirx);

        //to degree
        pitch = pitch * 180.0d / Math.PI;
        yaw = yaw * 180.0d / Math.PI;

        yaw += 90f;

        return new double[]{yaw, pitch};
    }
    //End credit to Kami

    public static void rotate(float yaw, float pitch) {
        MC.player.yaw = yaw;
        MC.player.pitch = pitch;
    }

    public static void rotate(double[] rotations) {
        MC.player.yaw = (float) rotations[0];
        MC.player.pitch = (float) rotations[1];
    }

    public static void lookAtBlock(BlockPos blockToLookAt) {
        rotate(calculateLookAt(blockToLookAt.getX(), blockToLookAt.getY(), blockToLookAt.getZ(), MC.player));
    }

    public static String vectorToString(Vec3d vector, boolean... includeY) {
        boolean reallyIncludeY = includeY.length <= 0 || includeY[0];
        StringBuilder builder = new StringBuilder();
        builder.append('(');
        builder.append((int) Math.floor(vector.x));
        builder.append(", ");
        if(reallyIncludeY) {
            builder.append((int) Math.floor(vector.y));
            builder.append(", ");
        }
        builder.append((int) Math.floor(vector.z));
        builder.append(")");
        return builder.toString();
    }

    public static List<Entity> getTargets(boolean players, boolean friends, boolean teammates, boolean passive, boolean hostile, boolean nametagged, boolean bots) {
        return StreamSupport.stream(MC.world.getEntities().spliterator(), false).filter(entity -> isTarget(entity, players, friends, teammates, passive, hostile, nametagged, bots)).collect(Collectors.toList());
    }

    public static boolean isTarget(Entity entity, boolean players, boolean friends, boolean teammates, boolean passive, boolean hostile, boolean nametagged, boolean bots) {
        if(!(entity instanceof LivingEntity) || entity == MC.player) return false;

        if(players && entity instanceof PlayerEntity) return true;
        if(friends && entity instanceof PlayerEntity && FriendManager.isFriend(((PlayerEntity) entity).getGameProfile().getName())) return true;
        if(teammates && entity.getScoreboardTeam() == MC.player.getScoreboardTeam() && MC.player.getScoreboardTeam() != null) return true;
        if(passive && isPassive(entity)) return true;
        if(hostile && isHostile(entity)) return true;
        if(nametagged && entity.hasCustomName()) return true;
        if(bots && isBot(entity)) return true;

        return false;
    }

    public static boolean isPassive(Entity entity) {
        if(entity instanceof IronGolemEntity && ((IronGolemEntity) entity).getAngryAt() == null) return true;
        else if(entity instanceof WolfEntity && (!((WolfEntity) entity).isAttacking() || ((WolfEntity) entity).getOwner() == MC.player)) return true;
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
}
