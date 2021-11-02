package dev.tigr.ares.forge.utils;

import dev.tigr.ares.Wrapper;
import dev.tigr.ares.core.util.Pair;
import dev.tigr.ares.forge.utils.entity.EntityUtils;
import dev.tigr.ares.forge.utils.entity.PlayerUtils;
import dev.tigr.ares.forge.utils.entity.SelfUtils;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityEnderCrystal;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Tigermouthbear
 */
public class WorldUtils implements Wrapper {
    /** Positional Getters */

    public static BlockPos roundBlockPos(Vec3d vec) {
        return new BlockPos(vec.x, (int) Math.round(vec.y), vec.z);
    }

    public static Pair<EnumFacing, Vec3d> getClosestVisibleSide(Vec3d pos, BlockPos blockPos) {
        List<EnumFacing> sides = getVisibleBlockSides(pos, blockPos);
        if(sides == null) return null;

        Vec3d center = MathUtils.ofCenterVec3i(blockPos);

        EnumFacing closestSide = null;
        Vec3d closestPos = null;
        for(EnumFacing side: sides) {
            Vec3d sidePos = center.add(MathUtils.ofVec3i(side.getDirectionVec())).scale(0.5);

            if(closestPos == null || MathUtils.squaredDistanceBetween(pos, sidePos) < MathUtils.squaredDistanceBetween(pos, closestPos)) {
                closestSide = side;
                closestPos = sidePos;
            }
        }

        return new Pair<>(closestSide, closestPos);
    }


    /** Entity List Getters */

    public static List<EntityPlayer> getPlayersInRadius(Vec3d center, double range) {
        return getEntitiesInRadius(EntityPlayer.class, center, range);
    }

    public static List<EntityPlayer> getPlayersInBox(BlockPos center, double expansion) {
        return getEntitiesInBox(EntityPlayer.class, center, expansion);
    }

    public static List<EntityEnderCrystal> getEndCrystalsInRadius(Vec3d center, double range) {
        return getEntitiesInRadius(EntityEnderCrystal.class, center, range);
    }

    public static List<EntityEnderCrystal> getEndCrystalsInBox(BlockPos center, double expansion) {
        return getEntitiesInBox(EntityEnderCrystal.class, center, expansion);
    }

    public static <T extends Entity> List<T> getEntitiesInRadius(Class<T> entityClass, Vec3d center, double range) {
        return getEntitiesInBox(entityClass, new BlockPos(center), range).stream().filter(entity -> MathUtils.isInRangeClosestPoint(center, entity.getEntityBoundingBox(), range)).collect(Collectors.toList());
    }

    public static <T extends Entity> List<T> getEntitiesInBox(Class<T> entityClass, BlockPos center, double expansion) {
        return new ArrayList<>(MC.world.getEntitiesWithinAABB(entityClass, new AxisAlignedBB(center).grow(expansion)));
    }

    public static List<Entity> getTargets(boolean players, boolean friends, boolean teammates, boolean passive, boolean hostile, boolean nametagged, boolean bots) {
        return MC.world.loadedEntityList.stream().filter(entity -> EntityUtils.isTarget(entity, players, friends, teammates, passive, hostile, nametagged, bots)).collect(Collectors.toList());
    }

    public static List<EntityPlayer> getPlayerTargets(double withinDistance) {
        List<EntityPlayer> targets = new ArrayList<>();

        targets.addAll(SelfUtils.getPlayersInRadius(withinDistance).stream().filter(player -> PlayerUtils.isValidTarget(player, withinDistance)).collect(Collectors.toList()));
        targets.sort(Comparators.entityDistance);

        return targets;
    }


    /** BlockPos / BlockEntity List Getters */

    public static List<BlockPos> getBlocksInReachDistance() {
        List<BlockPos> cube = new ArrayList<>();
        for(int x = -4; x <= 4; x++)
            for(int y = -4; y <= 4; y++)
                for(int z = -4; z <= 4; z++)
                    cube.add(MC.player.getPosition().add(x, y, z));

        return cube.stream().filter(pos -> MC.player.getDistanceSq(pos) <= 18.0625).collect(Collectors.toList());
    }


    /** Other Lists */

    public static List<EnumFacing> getVisibleBlockSides(Vec3d pos, BlockPos blockPos) {
        List<EnumFacing> sides = new ArrayList<>();

        if(pos.y > blockPos.getY()) sides.add(EnumFacing.UP);
        else sides.add(EnumFacing.DOWN);

        if(pos.x < blockPos.getX()) sides.add(EnumFacing.WEST);
        if(pos.x > blockPos.getX() + 1) sides.add(EnumFacing.EAST);

        if(pos.z < blockPos.getZ()) sides.add(EnumFacing.NORTH);
        if(pos.z > blockPos.getZ() +1) sides.add(EnumFacing.SOUTH);

        sides.removeIf(side -> !MC.world.getBlockState(blockPos.offset(side)).getMaterial().isReplaceable());

        if(!sides.isEmpty()) return sides;
        else return null;
    }


    /** State Utils */

    public static boolean canBreakBlock(BlockPos blockPos) {
        final IBlockState blockState = MC.world.getBlockState(blockPos);
        return blockState.getBlockHardness(MC.world, blockPos) != -1;
    }

    public static HoleType isHole(BlockPos pos) {
        IBlockState[] blockStates = new IBlockState[]{
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


    /** Others */

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






}
