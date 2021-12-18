package dev.tigr.ares.fabric.utils.entity;

import dev.tigr.ares.Wrapper;
import dev.tigr.ares.core.feature.module.modules.player.Freecam;
import dev.tigr.ares.core.util.math.floats.V2F;
import dev.tigr.ares.fabric.mixin.accessors.MinecraftClientAccessor;
import dev.tigr.ares.fabric.mixin.accessors.RenderTickCounterAccessor;
import dev.tigr.ares.fabric.utils.InventoryUtils;
import dev.tigr.ares.fabric.utils.MathUtils;
import dev.tigr.ares.fabric.utils.WorldUtils;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.FluidBlock;
import net.minecraft.block.ShapeContext;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.entity.effect.StatusEffectUtil;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
import net.minecraft.tag.FluidTags;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.*;

import java.util.List;

/**
 * Split from WorldUtils 10/17/21 - Makrennel
 */
public class SelfUtils implements Wrapper {
    // Gets whichever entity is where the actual player is right now
    public static PlayerEntity getPlayer() {
        if(Freecam.INSTANCE.getEnabled()) return (PlayerEntity) MC.world.getEntityById(Freecam.INSTANCE.clone);
        return MC.player;
    }


    /** Entity List Getters */

    public static List<PlayerEntity> getPlayersInRadius(double range) {
        return WorldUtils.getPlayersInRadius(getEyePos(), range);
    }

    public static List<PlayerEntity> getPlayersInBox(double expansion) {
        return WorldUtils.getPlayersInBox(new BlockPos(getEyePos()), expansion);
    }

    public static List<EndCrystalEntity> getEndCrystalsInRadius(double range) {
        return WorldUtils.getEndCrystalsInRadius(getEyePos(), range);
    }

    public static List<EndCrystalEntity> getEndCrystalsInBox(double expansion) {
        return WorldUtils.getEndCrystalsInBox(new BlockPos(getEyePos()), expansion);
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

    public static V2F calculateLookAtVector(double x, double y, double z) {
        double[] rotation = PlayerUtils.calculateLookFromPlayer(x, y, z, getPlayer());
        return new V2F((float) rotation[0], (float) rotation[1]);
    }

    public static V2F calculateLookAtVector(Vec3d pos) {
        double[] rotation = PlayerUtils.calculateLookFromPlayer(pos.x, pos.y, pos.z, getPlayer());
        return new V2F((float) rotation[0], (float) rotation[1]);
    }

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
                yaw = MC.player.prevYaw + (MC.player.getYaw() - MC.player.prevYaw) * ((RenderTickCounterAccessor) ((MinecraftClientAccessor) MC).getRenderTickCounter()).getTickTime();
        return MathUtils.getMovement(speed, forward, sideways, yaw);
    }

    public static float calcBlockBreakingDelta(BlockState state, int slot) {
        float f = state.getHardness(null, null);
        if(f == -1.0F) return 0.0F;
        else return getBlockBreakingSpeed(state, slot) / f / (InventoryUtils.canHarvestWithItemInSlot(state, slot) ? 30F : 100F);
    }

    public static float getBlockBreakingSpeed(BlockState block, int slot) {
        float f = MC.player.getInventory().main.get(slot).getMiningSpeedMultiplier(block);
        if(f > 1.0F) {
            ItemStack itemStack = MC.player.getInventory().getStack(slot);
            int i = EnchantmentHelper.getLevel(Enchantments.EFFICIENCY, itemStack);
            if(i > 0 && !itemStack.isEmpty()) f += (float)(i * i + 1);
        }

        if(StatusEffectUtil.hasHaste(MC.player)) f *= 1.0F + (float)(StatusEffectUtil.getHasteAmplifier(MC.player) + 1) * 0.2F;

        if(MC.player.hasStatusEffect(StatusEffects.MINING_FATIGUE)) {
            float k;
            switch(MC.player.getStatusEffect(StatusEffects.MINING_FATIGUE).getAmplifier()) {
                case 0:
                    k = 0.3F;
                    break;
                case 1:
                    k = 0.09F;
                    break;
                case 2:
                    k = 0.0027F;
                    break;
                case 3:
                default:
                    k = 8.1E-4F;
            }

            f *= k;
        }

        if(MC.player.isSubmergedIn(FluidTags.WATER) && !EnchantmentHelper.hasAquaAffinity(MC.player)) f /= 5.0F;

        if(!MC.player.isOnGround()) f /= 5.0F;

        return f;
    }
    

    /** Actions */

    // Full sequence of packets sent from MC.player.jump()
    public static void fakeJump(int firstPacket, int lastPacket) {
        if(firstPacket <= 0 && lastPacket >= 0) MC.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(MC.player.getX(), MC.player.getY(), MC.player.getZ(), true));
        if(firstPacket <= 1 && lastPacket >= 1) MC.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(MC.player.getX(), MC.player.getY() + 0.41999998688698, MC.player.getZ(), true));
        if(firstPacket <= 2 && lastPacket >= 2) MC.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(MC.player.getX(), MC.player.getY() + 0.7531999805212, MC.player.getZ(), true));
        if(firstPacket <= 3 && lastPacket >= 3) MC.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(MC.player.getX(), MC.player.getY() + 1.00133597911214, MC.player.getZ(), true));
        if(firstPacket <= 4 && lastPacket >= 4) MC.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(MC.player.getX(), MC.player.getY() + 1.16610926093821, MC.player.getZ(), true));
        if(firstPacket <= 5 && lastPacket >= 5) MC.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(MC.player.getX(), MC.player.getY() + 1.24918707874468, MC.player.getZ(), true));
        if(firstPacket <= 6 && lastPacket >= 6) MC.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(MC.player.getX(), MC.player.getY() + 1.17675927506424, MC.player.getZ(), true));
        if(firstPacket <= 7 && lastPacket >= 7) MC.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(MC.player.getX(), MC.player.getY() + 1.02442408821369, MC.player.getZ(), true));
        if(firstPacket <= 8 && lastPacket >= 8) MC.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(MC.player.getX(), MC.player.getY() + 0.79673560066871, MC.player.getZ(), true));
        if(firstPacket <= 9 && lastPacket >= 9) MC.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(MC.player.getX(), MC.player.getY() + 0.49520087700593, MC.player.getZ(), true));
        if(firstPacket <= 10 && lastPacket >= 10) MC.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(MC.player.getX(), MC.player.getY() + 0.1212968405392, MC.player.getZ(), true));
        if(firstPacket <= 11 && lastPacket >= 11) MC.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(MC.player.getX(), MC.player.getY(), MC.player.getZ(), true));
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
        MC.player.setPosition(xPos, MC.player.getY(), zPos);
        MC.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(MC.player.getX(), MC.player.getY(), MC.player.getZ(), MC.player.isOnGround()));
    }

    public static boolean placeBlockMainHand(BlockPos pos) {
        return placeBlockMainHand(false, -1, -1, false, false, pos);
    }
    public static boolean placeBlockMainHand(Boolean rotate, int rotationKey, int rotationPriority, boolean instantRotation, boolean instantBypassesCurrent, BlockPos pos) {
        return placeBlockMainHand(rotate, rotationKey, rotationPriority, instantRotation, instantBypassesCurrent, pos, true, false);
    }
    public static boolean placeBlockMainHand(Boolean rotate, int rotationKey, int rotationPriority, boolean instantRotation, boolean instantBypassesCurrent, BlockPos pos, Boolean airPlace, Boolean forceAirplace) {
        return placeBlockMainHand(rotate, rotationKey, rotationPriority, instantRotation, instantBypassesCurrent, pos, airPlace, forceAirplace, false);
    }
    public static boolean placeBlockMainHand(Boolean rotate, int rotationKey, int rotationPriority, boolean instantRotation, boolean instantBypassesCurrent, BlockPos pos, Boolean airPlace, Boolean forceAirplace, Boolean ignoreEntity) {
        return placeBlockMainHand(rotate, rotationKey, rotationPriority, instantRotation, instantBypassesCurrent, pos, airPlace, forceAirplace, ignoreEntity, null);
    }
    public static boolean placeBlockMainHand(Boolean rotate, int rotationKey, int rotationPriority, boolean instantRotation, boolean instantBypassesCurrent, BlockPos pos, Boolean airPlace, Boolean forceAirplace, Boolean ignoreEntity, Direction overrideSide) {
        return placeBlock(false, -1, rotate, rotationKey, rotationPriority, instantRotation, instantBypassesCurrent, Hand.MAIN_HAND, pos, airPlace, forceAirplace, ignoreEntity, overrideSide);
    }


    public static boolean placeBlockMainHand(boolean packetPlace, int slot, BlockPos pos) {
        return placeBlockMainHand(packetPlace, slot, false, -1, -1, false, false, pos);
    }
    public static boolean placeBlockMainHand(boolean packetPlace, int slot, Boolean rotate, int rotationKey, int rotationPriority, boolean instantRotation, boolean instantBypassesCurrent, BlockPos pos) {
        return placeBlockMainHand(packetPlace, slot, rotate, rotationKey, rotationPriority, instantRotation, instantBypassesCurrent, pos, true, false);
    }
    public static boolean placeBlockMainHand(boolean packetPlace, int slot, Boolean rotate, int rotationKey, int rotationPriority, boolean instantRotation, boolean instantBypassesCurrent, BlockPos pos, Boolean airPlace, Boolean forceAirplace) {
        return placeBlockMainHand(packetPlace, slot, rotate, rotationKey, rotationPriority, instantRotation, instantBypassesCurrent, pos, airPlace, forceAirplace, false);
    }
    public static boolean placeBlockMainHand(boolean packetPlace, int slot, Boolean rotate, int rotationKey, int rotationPriority, boolean instantRotation, boolean instantBypassesCurrent, BlockPos pos, Boolean airPlace, Boolean forceAirplace, Boolean ignoreEntity) {
        return placeBlockMainHand(packetPlace, slot, rotate, rotationKey, rotationPriority, instantRotation, instantBypassesCurrent, pos, airPlace, forceAirplace, ignoreEntity, null);
    }
    public static boolean placeBlockMainHand(boolean packetPlace, int slot, Boolean rotate, int rotationKey, int rotationPriority, boolean instantRotation, boolean instantBypassesCurrent, BlockPos pos, Boolean airPlace, Boolean forceAirplace, Boolean ignoreEntity, Direction overrideSide) {
        return placeBlock(packetPlace, slot, rotate, rotationKey, rotationPriority, instantRotation, instantBypassesCurrent, Hand.MAIN_HAND, pos, airPlace, forceAirplace, ignoreEntity, overrideSide);
    }


    public static boolean placeBlockNoRotate(Hand hand, BlockPos pos) {
        return placeBlock(false, -1, false, -1, -1, false, false, hand, pos, true, false);
    }
    public static boolean placeBlockNoRotate(boolean packetPlace, int slot, Hand hand, BlockPos pos) {
        return placeBlock(packetPlace, slot, false, -1, -1, false, false, hand, pos, true, false);
    }


    public static boolean placeBlock(Hand hand, BlockPos pos) {
        return placeBlock(false, -1, -1, false, false, hand, pos, true, false);
    }
    public static boolean placeBlock(Boolean rotate, int rotationKey, int rotationPriority, boolean instantRotation, boolean instantBypassesCurrent, Hand hand, BlockPos pos) {
        return placeBlock(rotate, rotationKey, rotationPriority, instantRotation, instantBypassesCurrent, hand, pos, false, false);
    }
    public static boolean placeBlock(Boolean rotate, int rotationKey, int rotationPriority, boolean instantRotation, boolean instantBypassesCurrent, Hand hand, BlockPos pos, Boolean airPlace, Boolean forceAirplace) {
        return placeBlock(rotate, rotationKey, rotationPriority, instantRotation, instantBypassesCurrent, hand, pos, airPlace, forceAirplace, false);
    }
    public static boolean placeBlock(Boolean rotate, int rotationKey, int rotationPriority, boolean instantRotation, boolean instantBypassesCurrent, Hand hand, BlockPos pos, Boolean airPlace, Boolean forceAirplace, Boolean ignoreEntity) {
        return placeBlock(rotate, rotationKey, rotationPriority, instantRotation, instantBypassesCurrent, hand, pos, airPlace, forceAirplace, ignoreEntity, null);
    }
    public static boolean placeBlock(Boolean rotate, int rotationKey, int rotationPriority, boolean instantRotation, boolean instantBypassesCurrent, Hand hand, BlockPos pos, Boolean airPlace, Boolean forceAirplace, Boolean ignoreEntity, Direction overrideSide) {
        return placeBlock(false, -1, rotate, rotationKey, rotationPriority, instantRotation, instantBypassesCurrent, hand, pos, airPlace, forceAirplace, ignoreEntity, overrideSide);
    }


    public static boolean placeBlock(boolean packetPlace, int slot, Hand hand, BlockPos pos) {
        return placeBlock(packetPlace, slot, false, -1, -1, false, false, hand, pos, true, false);
    }
    public static boolean placeBlock(boolean packetPlace, int slot, Boolean rotate, int rotationKey, int rotationPriority, boolean instantRotation, boolean instantBypassesCurrent, Hand hand, BlockPos pos) {
        return placeBlock(packetPlace, slot, rotate, rotationKey, rotationPriority, instantRotation, instantBypassesCurrent, hand, pos, false, false);
    }
    public static boolean placeBlock(boolean packetPlace, int slot, Boolean rotate, int rotationKey, int rotationPriority, boolean instantRotation, boolean instantBypassesCurrent, Hand hand, BlockPos pos, Boolean airPlace, Boolean forceAirplace) {
        return placeBlock(packetPlace, slot, rotate, rotationKey, rotationPriority, instantRotation, instantBypassesCurrent, hand, pos, airPlace, forceAirplace, false);
    }
    public static boolean placeBlock(boolean packetPlace, int slot, Boolean rotate, int rotationKey, int rotationPriority, boolean instantRotation, boolean instantBypassesCurrent, Hand hand, BlockPos pos, Boolean airPlace, Boolean forceAirplace, Boolean ignoreEntity) {
        return placeBlock(packetPlace, slot, rotate, rotationKey, rotationPriority, instantRotation, instantBypassesCurrent, hand, pos, airPlace, forceAirplace, ignoreEntity, null);
    }
    public static boolean placeBlock(boolean packetPlace, int slot, Boolean rotate, int rotationKey, int rotationPriority, boolean instantRotation, boolean instantBypassesCurrent, Hand hand, BlockPos pos, Boolean airPlace, Boolean forceAirplace, Boolean ignoreEntity, Direction overrideSide) {
        // make sure place is empty if ignoreEntity is not true
        if(ignoreEntity) {
            if(!MC.world.getBlockState(pos).getMaterial().isReplaceable())
                return false;
        } else if(!MC.world.getBlockState(pos).getMaterial().isReplaceable() || !MC.world.canPlace(Blocks.OBSIDIAN.getDefaultState(), pos, ShapeContext.absent()))
            return false;

        Vec3d eyesPos = new Vec3d(MC.player.getX(),
                MC.player.getY() + MC.player.getEyeHeight(MC.player.getPose()),
                MC.player.getZ());

        Vec3d hitVec = null;
        BlockPos neighbor = null;
        Direction side2 = null;

        if(!forceAirplace || !airPlace) {
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
        }

        // Air place if no neighbour was found
        if(airPlace) {
            if(hitVec == null) hitVec = Vec3d.ofCenter(pos);
            if(neighbor == null) neighbor = pos;
            if(side2 == null) side2 = Direction.UP;
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
                MC.player.getYaw()
                        + MathHelper.wrapDegrees(yaw - MC.player.getYaw()),
                MC.player.getPitch() + MathHelper
                        .wrapDegrees(pitch - MC.player.getPitch())};

        // Rotate using rotation manager and specified settings
        if(rotate)
            if(!ROTATIONS.setCurrentRotation(new V2F(rotations[0], rotations[1]), rotationKey, rotationPriority, instantRotation, instantBypassesCurrent))
                return false;

        MC.player.networkHandler.sendPacket(new ClientCommandC2SPacket(MC.player, ClientCommandC2SPacket.Mode.PRESS_SHIFT_KEY));

        int oldSlot = MC.player.getInventory().selectedSlot;
        if(slot != -1) {
            HOTBAR_TRACKER.setSlot(slot, packetPlace, oldSlot);
            // When packet placing we must send an update slot packet first
            if(packetPlace) HOTBAR_TRACKER.sendSlot();
        } else if(packetPlace) MC.player.networkHandler.sendPacket(new UpdateSelectedSlotC2SPacket(MC.player.getInventory().selectedSlot));

        if(packetPlace) MC.player.networkHandler.sendPacket(new PlayerInteractBlockC2SPacket(hand, new BlockHitResult(hitVec, side2, neighbor, false)));
        else MC.interactionManager.interactBlock(MC.player, MC.world, hand, new BlockHitResult(hitVec, side2, neighbor, false));

        MC.player.swingHand(hand);

        if(slot != -1) {
            if(!packetPlace) MC.player.getInventory().selectedSlot = oldSlot;
            HOTBAR_TRACKER.reset();
        }

        MC.player.networkHandler.sendPacket(new ClientCommandC2SPacket(MC.player, ClientCommandC2SPacket.Mode.RELEASE_SHIFT_KEY));

        return true;
    }

    public static void lookAtBlock(BlockPos blockToLookAt) {
        rotate(PlayerUtils.calculateLookFromPlayer(blockToLookAt.getX(), blockToLookAt.getY(), blockToLookAt.getZ(), MC.player));
    }

    public static void rotate(float yaw, float pitch) {
        MC.player.setYaw(yaw);
        MC.player.setPitch(pitch);
    }

    public static void rotate(double[] rotations) {
        MC.player.setYaw((float) rotations[0]);
        MC.player.setPitch((float) rotations[1]);
    }
}
