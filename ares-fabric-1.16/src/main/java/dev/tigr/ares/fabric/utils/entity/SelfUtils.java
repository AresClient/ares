package dev.tigr.ares.fabric.utils.entity;

import dev.tigr.ares.Wrapper;
import dev.tigr.ares.fabric.impl.modules.player.Freecam;
import dev.tigr.ares.fabric.mixin.accessors.MinecraftClientAccessor;
import dev.tigr.ares.fabric.mixin.accessors.RenderTickCounterAccessor;
import dev.tigr.ares.fabric.utils.MathUtils;
import dev.tigr.ares.fabric.utils.WorldUtils;
import net.minecraft.block.Blocks;
import net.minecraft.block.FluidBlock;
import net.minecraft.block.ShapeContext;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.*;

import java.util.List;

import static dev.tigr.ares.fabric.impl.modules.player.RotationManager.ROTATIONS;

/**
 * Split from WorldUtils 10/17/21 - Makrennel
 */
public class SelfUtils implements Wrapper {
    // Gets whichever entity is where the actual player is right now
    public static PlayerEntity getPlayer() {
        if(Freecam.INSTANCE.getEnabled()) return Freecam.INSTANCE.clone;
        return MC.player;
    }


    /** Entity List Getters */

    public static List<PlayerEntity> getPlayersInRadius(double range) {
        return WorldUtils.getPlayersInRadius(getPlayer().getPos(), range);
    }

    public static List<PlayerEntity> getPlayersInBox(double expansion) {
        return WorldUtils.getPlayersInBox(getPlayer().getBlockPos(), expansion);
    }

    public static List<EndCrystalEntity> getEndCrystalsInRadius(double range) {
        return WorldUtils.getEndCrystalsInRadius(getPlayer().getPos(), range);
    }

    public static List<EndCrystalEntity> getEndCrystalsInBox(double expansion) {
        return WorldUtils.getEndCrystalsInBox(getPlayer().getBlockPos(), expansion);
    }


    /** Positional Getters */

    // Gets the BlockPos of the player with all corrections applied
    public static BlockPos getBlockPosCorrected() {
        return EntityUtils.getBlockPosCorrected(getPlayer());
    }

    public static double getEyeY() {
        return PlayerUtils.getEyeY(getPlayer());
    }

    public static Vec3d getEyePos() {
        return PlayerUtils.getEyePos(getPlayer());
    }


    /** Calculation */

    public static double[] calculateLookAt(double x, double y, double z) {
        return PlayerUtils.calculateLookFromPlayer(x, y, z, getPlayer());
    }

    public static double[] calculateLookAt(Vec3d pos) {
        return PlayerUtils.calculateLookFromPlayer(pos.x, pos.y, pos.z, getPlayer());
    }

    public static double[] getMovement(final double speed) {
        float
                forward = MC.player.input.movementForward,
                sideways = MC.player.input.movementSideways,
                yaw = MC.player.prevYaw + (MC.player.yaw - MC.player.prevYaw) * ((RenderTickCounterAccessor) ((MinecraftClientAccessor) MC).getRenderTickCounter()).getTickTime();
        return MathUtils.getMovement(speed, forward, sideways, yaw);
    }


    /** Actions */

    // Full sequence of packets sent from MC.player.jump()
    public static void fakeJump(int firstPacket, int lastPacket) {
        if(firstPacket <= 0 && lastPacket >= 0) MC.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.PositionOnly(MC.player.getX(), MC.player.getY(), MC.player.getZ(), true));
        if(firstPacket <= 1 && lastPacket >= 1) MC.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.PositionOnly(MC.player.getX(), MC.player.getY() + 0.41999998688698, MC.player.getZ(), true));
        if(firstPacket <= 2 && lastPacket >= 2) MC.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.PositionOnly(MC.player.getX(), MC.player.getY() + 0.7531999805212, MC.player.getZ(), true));
        if(firstPacket <= 3 && lastPacket >= 3) MC.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.PositionOnly(MC.player.getX(), MC.player.getY() + 1.00133597911214, MC.player.getZ(), true));
        if(firstPacket <= 4 && lastPacket >= 4) MC.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.PositionOnly(MC.player.getX(), MC.player.getY() + 1.16610926093821, MC.player.getZ(), true));
        if(firstPacket <= 5 && lastPacket >= 5) MC.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.PositionOnly(MC.player.getX(), MC.player.getY() + 1.24918707874468, MC.player.getZ(), true));
        if(firstPacket <= 6 && lastPacket >= 6) MC.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.PositionOnly(MC.player.getX(), MC.player.getY() + 1.17675927506424, MC.player.getZ(), true));
        if(firstPacket <= 7 && lastPacket >= 7) MC.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.PositionOnly(MC.player.getX(), MC.player.getY() + 1.02442408821369, MC.player.getZ(), true));
        if(firstPacket <= 8 && lastPacket >= 8) MC.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.PositionOnly(MC.player.getX(), MC.player.getY() + 0.79673560066871, MC.player.getZ(), true));
        if(firstPacket <= 9 && lastPacket >= 9) MC.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.PositionOnly(MC.player.getX(), MC.player.getY() + 0.49520087700593, MC.player.getZ(), true));
        if(firstPacket <= 10 && lastPacket >= 10) MC.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.PositionOnly(MC.player.getX(), MC.player.getY() + 0.1212968405392, MC.player.getZ(), true));
        if(firstPacket <= 11 && lastPacket >= 11) MC.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.PositionOnly(MC.player.getX(), MC.player.getY(), MC.player.getZ(), true));
    }

    public static void snapPlayer() {
        BlockPos lastPos = MC.player.isOnGround() ? WorldUtils.roundBlockPos(MC.player.getPos()) : MC.player.getBlockPos();
        snapPlayer(lastPos);
    }

    public static void snapPlayer(BlockPos lastPos) {
        double xPos = MC.player.getPos().x;
        double zPos = MC.player.getPos().z;

        if(Math.abs((lastPos.getX() + 0.5) - MC.player.getPos().x) >= 0.2) {
            int xDir = (lastPos.getX() + 0.5) - MC.player.getPos().x > 0 ? 1 : -1;
            xPos += 0.3 * xDir;
        }

        if(Math.abs((lastPos.getZ() + 0.5) - MC.player.getPos().z) >= 0.2) {
            int zDir = (lastPos.getZ() + 0.5) - MC.player.getPos().z > 0 ? 1 : -1;
            zPos += 0.3 * zDir;
        }

        MC.player.setVelocity(0, 0, 0);
        MC.player.updatePosition(xPos, MC.player.getY(), zPos);
        MC.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.PositionOnly(MC.player.getX(), MC.player.getY(), MC.player.getZ(), MC.player.isOnGround()));
    }

    public static boolean placeBlockMainHand(BlockPos pos) {
        return placeBlockMainHand(false, -1, -1, false, false, pos);
    }

    public static boolean placeBlockMainHand(Boolean rotate, int rotationKey, int rotationPriority, boolean instantRotation, boolean instantBypassesCurrent, BlockPos pos) {
        return placeBlockMainHand(rotate, rotationKey, rotationPriority, instantRotation, instantBypassesCurrent, pos, true);
    }

    public static boolean placeBlockMainHand(Boolean rotate, int rotationKey, int rotationPriority, boolean instantRotation, boolean instantBypassesCurrent, BlockPos pos, Boolean airPlace) {
        return placeBlockMainHand(rotate, rotationKey, rotationPriority, instantRotation, instantBypassesCurrent, pos, airPlace, false);
    }

    public static boolean placeBlockMainHand(Boolean rotate, int rotationKey, int rotationPriority, boolean instantRotation, boolean instantBypassesCurrent, BlockPos pos, Boolean airPlace, Boolean ignoreEntity) {
        return placeBlockMainHand(rotate, rotationKey, rotationPriority, instantRotation, instantBypassesCurrent, pos, airPlace, ignoreEntity, null);
    }

    public static boolean placeBlockMainHand(Boolean rotate, int rotationKey, int rotationPriority, boolean instantRotation, boolean instantBypassesCurrent, BlockPos pos, Boolean airPlace, Boolean ignoreEntity, Direction overrideSide) {
        return placeBlock(rotate, rotationKey, rotationPriority, instantRotation, instantBypassesCurrent, Hand.MAIN_HAND, pos, airPlace, ignoreEntity, overrideSide);
    }

    public static boolean placeBlockNoRotate(Hand hand, BlockPos pos) {
        return placeBlock(false, -1, -1, false, false, hand, pos, true, false);
    }

    public static boolean placeBlock(Hand hand, BlockPos pos) {
        placeBlock(false, -1, -1, false, false, hand, pos, true);
        return true;
    }

    public static boolean placeBlock(Boolean rotate, int rotationKey, int rotationPriority, boolean instantRotation, boolean instantBypassesCurrent, Hand hand, BlockPos pos) {
        placeBlock(rotate, rotationKey, rotationPriority, instantRotation, instantBypassesCurrent, hand, pos, false);
        return true;
    }

    public static boolean placeBlock(Boolean rotate, int rotationKey, int rotationPriority, boolean instantRotation, boolean instantBypassesCurrent, Hand hand, BlockPos pos, Boolean airPlace) {
        placeBlock(rotate, rotationKey, rotationPriority, instantRotation, instantBypassesCurrent, hand, pos, airPlace, false);
        return true;
    }

    public static boolean placeBlock(Boolean rotate, int rotationKey, int rotationPriority, boolean instantRotation, boolean instantBypassesCurrent, Hand hand, BlockPos pos, Boolean airPlace, Boolean ignoreEntity) {
        placeBlock(rotate, rotationKey, rotationPriority, instantRotation, instantBypassesCurrent, hand, pos, airPlace, ignoreEntity, null);
        return true;
    }

    public static boolean placeBlock(Boolean rotate, int rotationKey, int rotationPriority, boolean instantRotation, boolean instantBypassesCurrent, Hand hand, BlockPos pos, Boolean airPlace, Boolean ignoreEntity, Direction overrideSide) {
        // make sure place is empty if ignoreEntity is not true
        if(ignoreEntity) {
            if (!MC.world.getBlockState(pos).getMaterial().isReplaceable())
                return false;
        } else if(!MC.world.getBlockState(pos).getMaterial().isReplaceable() || !MC.world.canPlace(Blocks.OBSIDIAN.getDefaultState(), pos, ShapeContext.absent()))
            return false;

        Vec3d eyesPos = new Vec3d(MC.player.getX(),
                MC.player.getY() + MC.player.getEyeHeight(MC.player.getPose()),
                MC.player.getZ());

        Vec3d hitVec = null;
        BlockPos neighbor = null;
        Direction side2 = null;

        if(overrideSide != null) {
            neighbor = pos.offset(overrideSide.getOpposite());
            side2 = overrideSide;
        }

        for(Direction side: Direction.values()) {
            if(overrideSide == null) {
                neighbor = pos.offset(side);
                side2 = side.getOpposite();

                // check if neighbor can be right clicked aka it isnt air
                if(MC.world.getBlockState(neighbor).isAir() || MC.world.getBlockState(neighbor).getBlock() instanceof FluidBlock) {
                    neighbor = null;
                    side2 = null;
                    continue;
                }
            }

            hitVec = new Vec3d(neighbor.getX(), neighbor.getY(), neighbor.getZ()).add(0.5, 0.5, 0.5).add(new Vec3d(side2.getUnitVector()).multiply(0.5));
            break;
        }

        // Air place if no neighbour was found
        if(airPlace) {
            if (hitVec == null) hitVec = new Vec3d(pos.getX(), pos.getY(), pos.getZ());
            if (neighbor == null) neighbor = pos;
            if (side2 == null) side2 = Direction.UP;
        } else if(hitVec == null || neighbor == null || side2 == null) {
            return false;
        }

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

        if(rotate)
            if(!ROTATIONS.setCurrentRotation(new Vec2f(rotations[0], rotations[1]), rotationKey, rotationPriority, instantRotation, instantBypassesCurrent))
                return false;

        MC.player.networkHandler.sendPacket(new ClientCommandC2SPacket(MC.player, ClientCommandC2SPacket.Mode.PRESS_SHIFT_KEY));
        MC.interactionManager.interactBlock(MC.player, MC.world, hand, new BlockHitResult(hitVec, side2, neighbor, false));
        MC.player.swingHand(hand);
        MC.player.networkHandler.sendPacket(new ClientCommandC2SPacket(MC.player, ClientCommandC2SPacket.Mode.RELEASE_SHIFT_KEY));

        return true;
    }

    public static void lookAtBlock(BlockPos blockToLookAt) {
        rotate(PlayerUtils.calculateLookFromPlayer(blockToLookAt.getX(), blockToLookAt.getY(), blockToLookAt.getZ(), MC.player));
    }

    public static void rotate(float yaw, float pitch) {
        MC.player.yaw = yaw;
        MC.player.pitch = pitch;
    }

    public static void rotate(double[] rotations) {
        MC.player.yaw = (float) rotations[0];
        MC.player.pitch = (float) rotations[1];
    }
}