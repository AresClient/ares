package dev.tigr.ares.forge.utils.entity;

import dev.tigr.ares.Wrapper;
import dev.tigr.ares.forge.impl.modules.player.Freecam;
import dev.tigr.ares.forge.utils.InventoryUtils;
import dev.tigr.ares.forge.utils.MathUtils;
import dev.tigr.ares.forge.utils.WorldUtils;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityEnderCrystal;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Enchantments;
import net.minecraft.init.MobEffects;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.CPacketAnimation;
import net.minecraft.network.play.client.CPacketEntityAction;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.*;

import java.util.List;

import static dev.tigr.ares.forge.impl.modules.player.RotationManager.ROTATIONS;

/**
 * Split from WorldUtils 10/17/21 - Makrennel
 */
public class SelfUtils implements Wrapper {
    // Gets whichever entity is where the actual player is right now
    public static EntityPlayer getPlayer() {
        if(Freecam.INSTANCE.getEnabled()) return Freecam.INSTANCE.clone;
        return MC.player;
    }

    /** Entity List Getters */

    public static List<EntityPlayer> getPlayersInRadius(double range) {
        return WorldUtils.getPlayersInRadius(getPlayer().getPositionVector(), range);
    }

    public static List<EntityPlayer> getPlayersInBox(double expansion) {
        return WorldUtils.getPlayersInBox(getPlayer().getPosition(), expansion);
    }

    public static List<EntityEnderCrystal> getEndCrystalsInRadius(double range) {
        return WorldUtils.getEndCrystalsInRadius(getPlayer().getPositionVector(), range);
    }

    public static List<EntityEnderCrystal> getEndCrystalsInBox(double expansion) {
        return WorldUtils.getEndCrystalsInBox(getPlayer().getPosition(), expansion);
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

    public static Vec2f calculateLookAtVector(double x, double y, double z) {
        double[] rotation = PlayerUtils.calculateLookFromPlayer(x, y, z, getPlayer());
        return new Vec2f((float) rotation[0], (float) rotation[1]);
    }

    public static Vec2f calculateLookAtVector(Vec3d pos) {
        double[] rotation = PlayerUtils.calculateLookFromPlayer(pos.x, pos.y, pos.z, getPlayer());
        return new Vec2f((float) rotation[0], (float) rotation[1]);
    }

    public static double[] calculateLookAt(double x, double y, double z) {
        return PlayerUtils.calculateLookFromPlayer(x, y, z, getPlayer());
    }

    public static double[] calculateLookAt(Vec3d pos) {
        return PlayerUtils.calculateLookFromPlayer(pos.x, pos.y, pos.z, getPlayer());
    }

    public static double[] getMovement(final double speed) {
        float
                forward = MC.player.movementInput.moveForward,
                sideways = MC.player.movementInput.moveStrafe,
                yaw = MC.player.prevRotationYaw + (MC.player.rotationYaw - MC.player.prevRotationYaw) * MC.getRenderPartialTicks();
        return MathUtils.getMovement(speed, forward, sideways, yaw);
    }

    public static float calcBlockBreakingDelta(IBlockState state, int slot) {
        float f = state.getBlockHardness(null, null);
        if(f < 0.0F) return 0.0F;
        else return getBlockBreakingSpeed(state, slot) / f / (InventoryUtils.canHarvestWithItemInSlot(state, slot) ? 30F : 100F);
    }

    public static float getBlockBreakingSpeed(IBlockState block, int slot) {
        float f = 1.0F;
        if(!MC.player.inventory.mainInventory.get(slot).isEmpty())
            f *= MC.player.inventory.mainInventory.get(slot).getDestroySpeed(block);

        if(f > 1.0F) {
            ItemStack itemstack = MC.player.inventory.getStackInSlot(slot);
            int i = EnchantmentHelper.getEnchantmentLevel(Enchantments.EFFICIENCY, itemstack);
            if(i > 0 && !itemstack.isEmpty()) f += (float)(i * i + 1);
        }

        if(MC.player.isPotionActive(MobEffects.HASTE)) f *= 1.0F + (float)(MC.player.getActivePotionEffect(MobEffects.HASTE).getAmplifier() + 1) * 0.2F;

        if(MC.player.isPotionActive(MobEffects.MINING_FATIGUE)) {
            float f1;

            switch (MC.player.getActivePotionEffect(MobEffects.MINING_FATIGUE).getAmplifier()) {
                case 0:
                    f1 = 0.3F;
                    break;
                case 1:
                    f1 = 0.09F;
                    break;
                case 2:
                    f1 = 0.0027F;
                    break;
                case 3:
                default:
                    f1 = 8.1E-4F;
            }

            f *= f1;
        }

        if(MC.player.isInsideOfMaterial(Material.WATER) && !EnchantmentHelper.getAquaAffinityModifier(MC.player)) f /= 5.0F;

        if(!MC.player.onGround) f /= 5.0F;

        return (f < 0 ? 0 : f);
    }


    /** Actions */

    // Full sequence of packets sent from MC.player.jump()
    public static void fakeJump(int firstPacket, int lastPacket) {
        if(firstPacket <= 0 && lastPacket >= 0) MC.player.connection.sendPacket(new CPacketPlayer.Position(MC.player.posX, MC.player.posY, MC.player.posZ, true));
        if(firstPacket <= 1 && lastPacket >= 1) MC.player.connection.sendPacket(new CPacketPlayer.Position(MC.player.posX, MC.player.posY + 0.41999998688698, MC.player.posZ, true));
        if(firstPacket <= 2 && lastPacket >= 2) MC.player.connection.sendPacket(new CPacketPlayer.Position(MC.player.posX, MC.player.posY + 0.7531999805212, MC.player.posZ, true));
        if(firstPacket <= 3 && lastPacket >= 3) MC.player.connection.sendPacket(new CPacketPlayer.Position(MC.player.posX, MC.player.posY + 1.00133597911214, MC.player.posZ, true));
        if(firstPacket <= 4 && lastPacket >= 4) MC.player.connection.sendPacket(new CPacketPlayer.Position(MC.player.posX, MC.player.posY + 1.16610926093821, MC.player.posZ, true));
        if(firstPacket <= 5 && lastPacket >= 5) MC.player.connection.sendPacket(new CPacketPlayer.Position(MC.player.posX, MC.player.posY + 1.24918707874468, MC.player.posZ, true));
        if(firstPacket <= 6 && lastPacket >= 6) MC.player.connection.sendPacket(new CPacketPlayer.Position(MC.player.posX, MC.player.posY + 1.17675927506424, MC.player.posZ, true));
        if(firstPacket <= 7 && lastPacket >= 7) MC.player.connection.sendPacket(new CPacketPlayer.Position(MC.player.posX, MC.player.posY + 1.02442408821369, MC.player.posZ, true));
        if(firstPacket <= 8 && lastPacket >= 8) MC.player.connection.sendPacket(new CPacketPlayer.Position(MC.player.posX, MC.player.posY + 0.79673560066871, MC.player.posZ, true));
        if(firstPacket <= 9 && lastPacket >= 9) MC.player.connection.sendPacket(new CPacketPlayer.Position(MC.player.posX, MC.player.posY + 0.49520087700593, MC.player.posZ, true));
        if(firstPacket <= 10 && lastPacket >= 10) MC.player.connection.sendPacket(new CPacketPlayer.Position(MC.player.posX, MC.player.posY + 0.1212968405392, MC.player.posZ, true));
        if(firstPacket <= 11 && lastPacket >= 11) MC.player.connection.sendPacket(new CPacketPlayer.Position(MC.player.posX, MC.player.posY, MC.player.posZ, true));
    }

    public static void snapPlayer() {
        BlockPos lastPos = MC.player.onGround ? WorldUtils.roundBlockPos(MC.player.getPositionVector()) : MC.player.getPosition();
        snapPlayer(lastPos);
    }

    public static void snapPlayer(BlockPos lastPos) {
        double xPos = MC.player.getPositionVector().x;
        double zPos = MC.player.getPositionVector().z;

        if(Math.abs((lastPos.getX() + 0.5) - MC.player.getPositionVector().x) >= 0.2) {
            int xDir = (lastPos.getX() + 0.5) - MC.player.getPositionVector().x > 0 ? 1 : -1;
            xPos += 0.3 * xDir;
        }

        if(Math.abs((lastPos.getZ() + 0.5) - MC.player.getPositionVector().z) >= 0.2) {
            int zDir = (lastPos.getZ() + 0.5) - MC.player.getPositionVector().z > 0 ? 1 : -1;
            zPos += 0.3 * zDir;
        }

        MC.player.motionX = MC.player.motionY = MC.player.motionZ = 0;
        MC.player.setPosition(xPos, MC.player.posY, zPos);
        MC.player.connection.sendPacket(new CPacketPlayer.Position(xPos, MC.player.posY, zPos, MC.player.onGround));
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
        return placeBlock(rotate, rotationKey, rotationPriority, instantRotation, instantBypassesCurrent, EnumHand.MAIN_HAND, pos, airPlace, ignoreEntity);
    }

    public static boolean placeBlockNoRotate(EnumHand hand, BlockPos pos) {
        return placeBlock(false, -1, -1, false, false, hand, pos, true, false);
    }

    public static boolean placeBlock(EnumHand hand, BlockPos pos) {
        placeBlock(false, -1, -1, false, false, hand, pos, true);
        return true;
    }

    public static boolean placeBlock(Boolean rotate, int rotationKey, int rotationPriority, boolean instantRotation, boolean instantBypassesCurrent, EnumHand hand, BlockPos pos) {
        placeBlock(rotate, rotationKey, rotationPriority, instantRotation, instantBypassesCurrent, hand, pos, false);
        return true;
    }

    public static boolean placeBlock(Boolean rotate, int rotationKey, int rotationPriority, boolean instantRotation, boolean instantBypassesCurrent, EnumHand hand, BlockPos pos, Boolean airPlace) {
        placeBlock(rotate, rotationKey, rotationPriority, instantRotation, instantBypassesCurrent, hand, pos, airPlace, false);
        return true;
    }

    public static boolean placeBlock(Boolean rotate, int rotationKey, int rotationPriority, boolean instantRotation, boolean instantBypassesCurrent, EnumHand hand, BlockPos pos, Boolean airPlace, Boolean ignoreEntity) {
        // make sure place is empty if ignoreEntity is not true
        if(ignoreEntity) {
            if(!MC.world.getBlockState(pos).getMaterial().isReplaceable())
                return false;
        } else if(!MC.world.getBlockState(pos).getMaterial().isReplaceable() ||
                    !MC.world.getEntitiesWithinAABBExcludingEntity(null, new AxisAlignedBB(pos)).stream().noneMatch(Entity::canBeCollidedWith))
            return false;

        Vec3d eyesPos = new Vec3d(MC.player.posX,
                MC.player.posY + MC.player.getEyeHeight(),
                MC.player.posZ);

        Vec3d hitVec = null;
        BlockPos neighbor = null;
        EnumFacing side2 = null;
        for(EnumFacing side: EnumFacing.values()) {
            neighbor = pos.offset(side);
            side2 = side.getOpposite();

            // check if neighbor can be right clicked
            if(!MC.world.getBlockState(neighbor).getBlock().canCollideCheck(MC.world.getBlockState(neighbor), false)) {
                neighbor = null;
                side2 = null;
                continue;
            }

            hitVec = new Vec3d(neighbor).add(0.5, 0.5, 0.5).add(new Vec3d(side2.getDirectionVec()).scale(0.5));
            break;
        }

        // Air place if no neighbour was found
        if(airPlace) {
            if(hitVec == null) hitVec = new Vec3d(pos.getX(), pos.getY(), pos.getZ());
            if(neighbor == null) neighbor = pos;
            if(side2 == null) side2 = EnumFacing.UP;
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
                MC.player.rotationYaw
                        + MathHelper.wrapDegrees(yaw - MC.player.rotationYaw),
                MC.player.rotationPitch + MathHelper
                        .wrapDegrees(pitch - MC.player.rotationPitch)};

        if(rotate)
            if(!ROTATIONS.setCurrentRotation(new Vec2f(rotations[0], rotations[1]), rotationKey, rotationPriority, instantRotation, instantBypassesCurrent))
                return false;

        MC.player.connection.sendPacket(new CPacketEntityAction(MC.player, CPacketEntityAction.Action.START_SNEAKING));
        MC.playerController.processRightClickBlock(MC.player, MC.world, neighbor, side2, hitVec, hand);
        MC.player.connection.sendPacket(new CPacketAnimation(EnumHand.MAIN_HAND));
        MC.player.connection.sendPacket(new CPacketEntityAction(MC.player, CPacketEntityAction.Action.STOP_SNEAKING));

        return true;
    }

    public static void lookAtBlock(BlockPos blockToLookAt) {
        rotate(PlayerUtils.calculateLookFromPlayer(blockToLookAt.getX(), blockToLookAt.getY(), blockToLookAt.getZ(), MC.player));
    }

    public static void rotate(float yaw, float pitch) {
        MC.player.rotationYaw = yaw;
        MC.player.rotationPitch = pitch;
    }

    public static void rotate(double[] rotations) {
        MC.player.rotationYaw = (float) rotations[0];
        MC.player.rotationPitch = (float) rotations[1];
    }
}
