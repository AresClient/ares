package dev.tigr.ares.forge.impl.modules.combat;

import dev.tigr.ares.core.feature.FriendManager;
import dev.tigr.ares.core.feature.module.Category;
import dev.tigr.ares.core.feature.module.Module;
import dev.tigr.ares.core.setting.Setting;
import dev.tigr.ares.core.setting.settings.BooleanSetting;
import dev.tigr.ares.core.setting.settings.numerical.DoubleSetting;
import dev.tigr.ares.core.util.render.TextColor;
import dev.tigr.ares.forge.impl.modules.exploit.InstantMine;
import dev.tigr.ares.forge.utils.Comparators;
import dev.tigr.ares.forge.utils.WorldUtils;
import dev.tigr.ares.forge.utils.entity.PlayerUtils;
import net.minecraft.block.BlockAir;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemPickaxe;
import net.minecraft.network.play.client.CPacketHeldItemChange;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.network.play.client.CPacketPlayerDigging;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Tigermouthbear 12/12/20
 */
@Module.Info(name = "AutoCity", description = "Automatically mines closest players surround", category = Category.COMBAT)
public class AutoCity extends Module {
    private final Setting<Double> range = register(new DoubleSetting("Range", 5, 0, 10));
    private final Setting<Boolean> rotate = register(new BooleanSetting("Rotate", true));
    private final Setting<Boolean> instant = register(new BooleanSetting("Instant", true));
    private final Setting<Boolean> oneDotThirteen = register(new BooleanSetting("1.13+", false));

    private boolean toggleInstant = false;
    
    @Override
    public void onEnable() {
        if (instant.getValue() && !InstantMine.INSTANCE.getEnabled()) {
            toggleInstant = true;
            InstantMine.INSTANCE.setEnabled(true);
        }
        // get targets
        for(EntityPlayer playerEntity: WorldUtils.getPlayerTargets(range.getValue() +2)) {
            Vec3d posVec = playerEntity.getPositionVector();
            BlockPos pos = new BlockPos(Math.floor(posVec.x), Math.round(posVec.y), Math.floor(posVec.z));
            if(inCity(pos)) {
                // find block
                List<BlockPos> blocks = Arrays.asList(pos.north(), pos.east(), pos.south(), pos.west());
                blocks.sort(Comparators.blockDistance);
                BlockPos target = null;
                for(BlockPos block: blocks) {
                    if(!inPlayerCity(block) && MC.world.getBlockState(block).getBlock() != Blocks.BEDROCK && MC.player.getDistanceSq(block.getX() + 0.5, block.getY() + 0.5, block.getZ() + 0.5) < range.getValue() * range.getValue()) {
                        if (shouldBreakCheck(block, pos)) {
                            target = block;
                            break;
                        }
                    }
                }
                if(target == null) continue;

                // find pick
                int index = -1;
                for(int i = 0; i < 9; i++) {
                    if(MC.player.inventory.getStackInSlot(i).getItem() instanceof ItemPickaxe) {
                        index = i;
                        break;
                    }
                }
                if(index == -1) UTILS.printMessage("No pickaxe in hotbar!");
                else {
                    // switch to pick
                    MC.player.inventory.currentItem = index;
                    MC.player.connection.sendPacket(new CPacketHeldItemChange(index));

                    // rotate
                    if (rotate.getValue()) {
                        double[] rotations = PlayerUtils.calculateLookFromPlayer(target.getX() + 0.5, target.getY() + 0.5, target.getZ() + 0.5, MC.player);
                        MC.player.connection.sendPacket(new CPacketPlayer.Rotation((float) rotations[0], (float) rotations[1], MC.player.onGround));
                    }

                    // break
                    MC.player.connection.sendPacket(new CPacketPlayerDigging(CPacketPlayerDigging.Action.START_DESTROY_BLOCK, target, EnumFacing.UP));
                    MC.player.swingArm(EnumHand.MAIN_HAND);
                    if (instant.getValue()) MC.playerController.onPlayerDamageBlock(new BlockPos(target.getX(), target.getY(), target.getZ()), EnumFacing.UP);
                    MC.player.connection.sendPacket(new CPacketPlayerDigging(CPacketPlayerDigging.Action.STOP_DESTROY_BLOCK, target, EnumFacing.UP));

                }
                if(!toggleInstant) setEnabled(false);
                return;
            }
        }
        UTILS.printMessage(TextColor.RED + "Could not find a target!");
        if(!toggleInstant) setEnabled(false);
    }

    @Override
    public void onDisable(){
        if (toggleInstant) {
            toggleInstant = false;
            InstantMine.INSTANCE.setEnabled(false);
        }
    }
    
    private boolean inCity(BlockPos pos) {
        return allBlocks(pos.north(), pos.east(), pos.south(), pos.west());
    }

    private boolean inPlayerCity(BlockPos pos) {
        BlockPos current = MC.player.getPosition();
        return pos.north() == current || pos.east() == current || pos.south() == current || pos.west() == current;
    }
    
    private boolean allBlocks(BlockPos... pos) {
        return Arrays.stream(pos).allMatch(blockPos -> MC.world.getBlockState(blockPos) != Blocks.AIR);
    }

    private boolean shouldBreakCheck(BlockPos pos, BlockPos target) {
        if(oneDotThirteen.getValue()) return true;
        else if(MC.world.getBlockState(pos.up()).getBlock() instanceof BlockAir) return true;
        else if(pos.equals(target.north())) {
            if(oneTwelveCheck(pos.north())) return true;
            else if(oneTwelveCheck(pos.east())) return true;
            else return oneTwelveCheck(pos.west());
        }
        else if(pos.equals(target.east())) {
            if(oneTwelveCheck(pos.east())) return true;
            else if(oneTwelveCheck(pos.north())) return true;
            else return oneTwelveCheck(pos.south());
        }
        else if(pos.equals(target.south())) {
            if(oneTwelveCheck(pos.south())) return true;
            else if(oneTwelveCheck(pos.east())) return true;
            else return oneTwelveCheck(pos.west());
        }
        else if(pos.equals(target.west())) {
            if(oneTwelveCheck(pos.west())) return true;
            else if(oneTwelveCheck(pos.south())) return true;
            else return oneTwelveCheck(pos.north());
        }
        else return false;
    }

    private boolean oneTwelveCheck(BlockPos pos) {
        return MC.world.getBlockState(pos).getBlock() instanceof BlockAir && MC.world.getBlockState(pos.up()).getBlock() instanceof BlockAir;
    }
}
