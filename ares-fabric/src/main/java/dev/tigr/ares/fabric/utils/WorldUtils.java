package dev.tigr.ares.fabric.utils;

import dev.tigr.ares.Wrapper;
import dev.tigr.ares.core.util.Pair;
import dev.tigr.ares.fabric.utils.entity.EntityUtils;
import dev.tigr.ares.fabric.utils.entity.PlayerUtils;
import dev.tigr.ares.fabric.utils.entity.SelfUtils;
import io.netty.buffer.Unpooled;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket;
import net.minecraft.util.math.*;
import net.minecraft.world.chunk.WorldChunk;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * @author Tigermouthbear 9/26/20
 */
public class WorldUtils implements Wrapper {
    /** Positional Getters */

    public static BlockPos roundBlockPos(Vec3d vec) {
        return new BlockPos(vec.x, (int) Math.round(vec.y), vec.z);
    }

    public static Pair<Direction, Vec3d> getClosestVisibleSide(Vec3d pos, BlockPos blockPos) {
        List<Direction> sides = getVisibleBlockSides(pos, blockPos);
        if(sides == null) return null;

        Vec3d center = Vec3d.ofCenter(blockPos);

        Direction closestSide = null;
        Vec3d closestPos = null;
        for(Direction side: sides) {
            Vec3d sidePos = center.add(Vec3d.of(side.getVector()).multiply(0.5));

            if(closestPos == null || MathUtils.squaredDistanceBetween(pos, sidePos) < MathUtils.squaredDistanceBetween(pos, closestPos)) {
                closestSide = side;
                closestPos = sidePos;
            }
        }

        return new Pair<>(closestSide, closestPos);
    }


    /** Entity List Getters */

    public static List<PlayerEntity> getPlayersInRadius(Vec3d center, double range) {
        return getEntitiesInRadius(PlayerEntity.class, center, range);
    }

    public static List<PlayerEntity> getPlayersInBox(BlockPos center, double expansion) {
        return getEntitiesInBox(PlayerEntity.class, center, expansion);
    }

    public static List<EndCrystalEntity> getEndCrystalsInRadius(Vec3d center, double range) {
        return getEntitiesInRadius(EndCrystalEntity.class, center, range);
    }

    public static List<EndCrystalEntity> getEndCrystalsInBox(BlockPos center, double expansion) {
        return getEntitiesInBox(EndCrystalEntity.class, center, expansion);
    }

    public static <T extends Entity> List<T> getEntitiesInRadius(Class<T> entityClass, Vec3d center, double range) {
        return MC.world.getNonSpectatingEntities(entityClass, new Box(new BlockPos(center)).expand(range)).stream().filter(entity -> MathUtils.isInRangeClosestPoint(center, entity.getBoundingBox(), range)).collect(Collectors.toList());
    }

    public static <T extends Entity> List<T> getEntitiesInBox(Class<T> entityClass, BlockPos center, double expansion) {
        return MC.world.getNonSpectatingEntities(entityClass, new Box(center).expand(expansion));
    }

    public static List<Entity> getTargets(boolean players, boolean friends, boolean teammates, boolean passive, boolean hostile, boolean nametagged, boolean bots) {
        return StreamSupport.stream(MC.world.getEntities().spliterator(), false).filter(entity -> EntityUtils.isTarget(entity, players, friends, teammates, passive, hostile, nametagged, bots)).collect(Collectors.toList());
    }

    public static List<PlayerEntity> getPlayerTargets(double withinDistance) {
        List<PlayerEntity> targets = new ArrayList<>();

        targets.addAll(SelfUtils.getPlayersInRadius(withinDistance).stream().filter(player -> PlayerUtils.isValidTarget(player, withinDistance)).collect(Collectors.toList()));
        targets.sort(Comparators.entityDistance);

        return targets;
    }


    /** BlockPos / BlockEntity List Getters */

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

    public static List<BlockEntity> getBlockEntities() {
        List<BlockEntity> blockEntities = new ArrayList<>();
        ChunkPos chunkPos = MC.player.getChunkPos();
        int viewDistance = MC.options.viewDistance;
        for(int x = -viewDistance; x <= viewDistance; x++) {
            for(int z = -viewDistance; z <= viewDistance; z++) {
                WorldChunk worldChunk = MC.world.getChunkManager().getWorldChunk(chunkPos.x + x, chunkPos.z + z);
                if(worldChunk != null) blockEntities.addAll(worldChunk.getBlockEntities().values());
            }
        }

        return blockEntities;
    }


    /** Other Lists */

    public static final List<Block> NONSOLID_BLOCKS = Arrays.asList(
            Blocks.AIR, Blocks.LAVA, Blocks.WATER, Blocks.GRASS,
            Blocks.VINE, Blocks.SEAGRASS, Blocks.TALL_SEAGRASS,
            Blocks.SNOW, Blocks.TALL_GRASS, Blocks.FIRE, Blocks.VOID_AIR
    );

    public static List<Direction> getVisibleBlockSides(Vec3d pos, BlockPos blockPos) {
        List<Direction> sides = new ArrayList<>();

        if(pos.y > blockPos.getY()) sides.add(Direction.UP);
        else sides.add(Direction.DOWN);

        if(pos.x < blockPos.getX()) sides.add(Direction.WEST);
        if(pos.x > blockPos.getX() + 1) sides.add(Direction.EAST);

        if(pos.z < blockPos.getZ()) sides.add(Direction.NORTH);
        if(pos.z > blockPos.getZ() +1) sides.add(Direction.SOUTH);

        sides.removeIf(side -> !MC.world.getBlockState(blockPos.offset(side)).getMaterial().isReplaceable());

        if(!sides.isEmpty()) return sides;
        else return null;
    }


    /** State Utils */

    public static boolean canReplace(BlockPos pos) {
        return NONSOLID_BLOCKS.contains(MC.world.getBlockState(pos).getBlock()) && MC.world.getOtherEntities(null, new Box(pos)).stream().noneMatch(Entity::collides);
    }

    public static boolean canBreakBlock(BlockPos blockPos) {
        final BlockState blockState = MC.world.getBlockState(blockPos);
        return blockState.getHardness(MC.world, blockPos) != -1;
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

    public static enum InteractType {INTERACT, ATTACK, INTERACT_AT}
    public static Pair<InteractType, Integer> getInteractData(PlayerInteractEntityC2SPacket packet) {
        PacketByteBuf buffer = new PacketByteBuf(Unpooled.buffer());
        packet.write(buffer);
        int id = buffer.readVarInt();
        return new Pair<InteractType, Integer>(buffer.readEnumConstant(InteractType.class), id);
    }
}
