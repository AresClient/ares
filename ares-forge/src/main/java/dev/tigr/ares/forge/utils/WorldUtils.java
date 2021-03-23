package dev.tigr.ares.forge.utils;

import dev.tigr.ares.core.feature.FriendManager;
import dev.tigr.ares.core.util.render.TextColor;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityAgeable;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.entity.monster.EntityEnderman;
import net.minecraft.entity.monster.EntityIronGolem;
import net.minecraft.entity.monster.EntityPigZombie;
import net.minecraft.entity.passive.EntityAmbientCreature;
import net.minecraft.entity.passive.EntitySquid;
import net.minecraft.entity.passive.EntityWolf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.network.play.client.CPacketEntityAction;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.network.play.client.CPacketPlayerTryUseItemOnBlock;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static dev.tigr.ares.CoreWrapper.UTILS;
import static dev.tigr.ares.Wrapper.MC;

/**
 * @author Tigermouthbear
 */
public class WorldUtils {
    public static boolean placeBlockMainHand(BlockPos pos) {
        return placeBlockMainHand(pos, true, false);
    }

    public static boolean placeBlockMainHand(BlockPos pos, Boolean rotate) {
        return placeBlockMainHand(pos, rotate, false);
    }

    public static boolean placeBlockMainHand(BlockPos pos, Boolean rotate, Boolean ignoreEntity) {
        return placeBlock(EnumHand.MAIN_HAND, pos, rotate, ignoreEntity);
    }

    public static boolean placeBlockNoRotate(EnumHand hand, BlockPos pos) {
        return placeBlock(hand, pos, false, false);
    }

    public static boolean placeBlock(EnumHand hand, BlockPos pos) {
        placeBlock(hand, pos, true, false);
        return true;
    }
    public static boolean placeBlock(EnumHand hand, BlockPos pos, Boolean rotate) {
        placeBlock(hand, pos, rotate, false);
        return true;
    }
    public static boolean placeBlock(EnumHand hand, BlockPos pos, Boolean rotate, Boolean ignoreEntity) {
        // make sure place is empty if ignoreEntity is not true
        if(ignoreEntity) {
            if (!MC.world.getBlockState(pos).getMaterial().isReplaceable())
                return false;
        } else if (!MC.world.getBlockState(pos).getMaterial().isReplaceable() ||
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
        if(hitVec == null) hitVec = new Vec3d(pos.getX(), pos.getY(), pos.getZ());
        if(neighbor == null) neighbor = pos;
        if(side2 == null) side2 = EnumFacing.UP;

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

        if (rotate) MC.player.connection.sendPacket(new CPacketPlayer.Rotation(rotations[0], rotations[1], MC.player.onGround));

        MC.player.connection.sendPacket(new CPacketEntityAction(MC.player, CPacketEntityAction.Action.START_SNEAKING));
        MC.playerController.processRightClickBlock(MC.player, MC.world, neighbor, side2, hitVec, hand);
        MC.player.swingArm(hand);
        MC.player.connection.sendPacket(new CPacketEntityAction(MC.player, CPacketEntityAction.Action.STOP_SNEAKING));

        return true;
    }

    //Credit to KAMI for code below
    public static double[] calculateLookAt(double px, double py, double pz, EntityPlayer me) {
        double dirx = me.posX - px;
        double diry = me.posY + me.getEyeHeight() - py;
        double dirz = me.posZ - pz;

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
        MC.player.rotationYaw = yaw;
        MC.player.rotationPitch = pitch;
    }

    public static void rotate(double[] rotations) {
        MC.player.rotationYaw = (float) rotations[0];
        MC.player.rotationPitch = (float) rotations[1];
    }

    public static void lookAtBlock(BlockPos blockToLookAt) {
        rotate(calculateLookAt(blockToLookAt.getX(), blockToLookAt.getY(), blockToLookAt.getZ(), MC.player));
    }

    public static List<BlockPos> getBlocksInReachDistance() {
        List<BlockPos> cube = new ArrayList<>();
        for(int x = -4; x <= 4; x++)
            for(int y = -4; y <= 4; y++)
                for(int z = -4; z <= 4; z++)
                    cube.add(MC.player.getPosition().add(x, y, z));

        return cube.stream().filter(pos -> MC.player.getDistanceSq(pos) <= 18.0625).collect(Collectors.toList());
    }

    public static void moveEntityWithSpeed(Entity entity, double speed, boolean shouldMoveY) {
        float yaw = (float) Math.toRadians(MC.player.rotationYaw);

        if(MC.gameSettings.keyBindForward.isKeyDown()) {
            entity.motionX = -(MathHelper.sin(yaw) * speed);
            entity.motionZ = MathHelper.cos(yaw) * speed;
        } else if(MC.gameSettings.keyBindBack.isKeyDown()) {
            entity.motionX = MathHelper.sin(yaw) * speed;
            entity.motionZ = -(MathHelper.cos(yaw) * speed);
        }

        if(MC.gameSettings.keyBindLeft.isKeyDown()) {
            entity.motionZ = MathHelper.sin(yaw) * speed;
            entity.motionX = MathHelper.cos(yaw) * speed;
        } else if(MC.gameSettings.keyBindRight.isKeyDown()) {
            entity.motionZ = -(MathHelper.sin(yaw) * speed);
            entity.motionX = -(MathHelper.cos(yaw) * speed);
        }

        if(shouldMoveY) {
            if(MC.gameSettings.keyBindJump.isKeyDown()) {
                entity.motionY = speed;
            } else if(MC.gameSettings.keyBindSneak.isKeyDown()) {
                entity.motionY = -speed;
            }
        }

        //strafe
        if(MC.gameSettings.keyBindForward.isKeyDown() && MC.gameSettings.keyBindLeft.isKeyDown()) {
            entity.motionX = (MathHelper.cos(yaw) * speed) - (MathHelper.sin(yaw) * speed);
            entity.motionZ = (MathHelper.cos(yaw) * speed) + (MathHelper.sin(yaw) * speed);
        } else if(MC.gameSettings.keyBindLeft.isKeyDown() && MC.gameSettings.keyBindBack.isKeyDown()) {
            entity.motionX = (MathHelper.cos(yaw) * speed) + (MathHelper.sin(yaw) * speed);
            entity.motionZ = -(MathHelper.cos(yaw) * speed) + (MathHelper.sin(yaw) * speed);
        } else if(MC.gameSettings.keyBindBack.isKeyDown() && MC.gameSettings.keyBindRight.isKeyDown()) {
            entity.motionX = -(MathHelper.cos(yaw) * speed) + (MathHelper.sin(yaw) * speed);
            entity.motionZ = -(MathHelper.cos(yaw) * speed) - (MathHelper.sin(yaw) * speed);
        } else if(MC.gameSettings.keyBindRight.isKeyDown() && MC.gameSettings.keyBindForward.isKeyDown()) {
            entity.motionX = -(MathHelper.cos(yaw) * speed) - (MathHelper.sin(yaw) * speed);
            entity.motionZ = (MathHelper.cos(yaw) * speed) - (MathHelper.sin(yaw) * speed);
        }
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
        return MC.world.loadedEntityList.stream().filter(entity -> isTarget(entity, players, friends, teammates, passive, hostile, nametagged, bots)).collect(Collectors.toList());
    }

    public static boolean isTarget(Entity entity, boolean players, boolean friends, boolean teammates, boolean passive, boolean hostile, boolean nametagged, boolean bots) {
        if(!(entity instanceof EntityLivingBase) || entity == MC.player) return false;

        if(players && entity instanceof EntityPlayer) return true;
        if(friends && entity instanceof EntityPlayer && FriendManager.isFriend(((EntityPlayer) entity).getGameProfile().getName())) return true;
        if(teammates && entity.getTeam() == MC.player.getTeam() && MC.player.getTeam() != null) return true;
        if(passive && isPassive(entity)) return true;
        if(hostile && isHostile(entity)) return true;
        if(nametagged && entity.hasCustomName()) return true;
        if(bots && isBot(entity)) return true;

        return false;
    }

    public static boolean isPassive(Entity entity) {
        if(entity instanceof EntityIronGolem && ((EntityIronGolem) entity).getRevengeTarget() == null) return true;
        else if(entity instanceof EntityWolf && (!((EntityWolf) entity).isAngry() || ((EntityWolf) entity).getOwner() == MC.player)) return true;
        else return entity instanceof EntityAmbientCreature || entity instanceof EntityAgeable || entity instanceof EntitySquid;
    }

    public static boolean isHostile(Entity entity) {
        if(entity instanceof EntityIronGolem) return ((EntityIronGolem) entity).getRevengeTarget() == MC.player && ((EntityIronGolem) entity).getRevengeTarget() != null;
        else if(entity instanceof EntityWolf) return ((EntityWolf) entity).isAngry() && ((EntityWolf) entity).getOwner() != MC.player;
        else if(entity instanceof EntityPigZombie) return ((EntityPigZombie) entity).isAngry() || ((EntityPigZombie) entity).isArmsRaised();
        else if(entity instanceof EntityEnderman) return ((EntityEnderman) entity).isScreaming();
        return entity.isCreatureType(EnumCreatureType.MONSTER, false);
    }

    public static boolean isBot(Entity entity) {
        return entity instanceof EntityPlayer && entity.isInvisibleToPlayer(MC.player) && !entity.onGround && entity.isAirBorne && !entity.canBeCollidedWith();
    }

    // must be done onTick - put this here because there may be other uses for fakeJump elsewhere, but it can just be put right into burrow if not.
    public static void fakeJump() {
        MC.player.connection.sendPacket(new CPacketPlayer.Position(MC.player.posX, MC.player.posY + 0.40, MC.player.posZ, true));
        MC.player.connection.sendPacket(new CPacketPlayer.Position(MC.player.posX, MC.player.posY + 0.75, MC.player.posZ, true));
        MC.player.connection.sendPacket(new CPacketPlayer.Position(MC.player.posX, MC.player.posY + 1.01, MC.player.posZ, true));
        MC.player.connection.sendPacket(new CPacketPlayer.Position(MC.player.posX, MC.player.posY + 1.15, MC.player.posZ, true));
    }
}
