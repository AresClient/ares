package dev.tigr.ares.fabric.utils;

import dev.tigr.ares.Wrapper;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Tigermouthbear 9/26/20
 */
public class WorldUtils implements Wrapper {
    public static void placeBlockMainHand(BlockPos pos) {
        placeBlock(Hand.MAIN_HAND, pos);
    }

    public static void placeBlock(Hand hand, BlockPos pos) {
        Vec3d eyesPos = new Vec3d(MC.player.getX(),
                MC.player.getY() + MC.player.getEyeHeight(MC.player.getPose()),
                MC.player.getZ());

        for(Direction side: Direction.values()) {
            BlockPos neighbor = pos.offset(side);
            Direction side2 = side.getOpposite();

            // check if neighbor can be right clicked
            if(!MC.player.canPlaceOn(pos, side, MC.player.getStackInHand(hand)))
                continue;

            Vec3d hitVec = new Vec3d(neighbor.getX(), neighbor.getY(), neighbor.getZ()).add(0.5, 0.5, 0.5)
                    .add(new Vec3d(side2.getUnitVector()).multiply(0.5));

            // check if hitVec is within range (4.25 blocks)
            if(eyesPos.squaredDistanceTo(hitVec) > 18.0625)
                continue;

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

            // make sure can place there
            BlockHitResult blockHitResult = new BlockHitResult(hitVec, side, neighbor, false);
            if(!MC.world.getBlockState(pos).canReplace(new ItemPlacementContext(MC.player, hand, MC.player.getStackInHand(hand), blockHitResult))) return;

            MC.getNetworkHandler().sendPacket(new PlayerMoveC2SPacket.LookOnly(rotations[0], rotations[1], MC.player.isOnGround()));
            MC.player.networkHandler.sendPacket(new ClientCommandC2SPacket(MC.player, ClientCommandC2SPacket.Mode.PRESS_SHIFT_KEY));
            MC.interactionManager.interactBlock(MC.player, MC.world, hand, blockHitResult);
            MC.player.swingHand(hand);
            MC.player.networkHandler.sendPacket(new ClientCommandC2SPacket(MC.player, ClientCommandC2SPacket.Mode.RELEASE_SHIFT_KEY));

            return;
        }
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
}
