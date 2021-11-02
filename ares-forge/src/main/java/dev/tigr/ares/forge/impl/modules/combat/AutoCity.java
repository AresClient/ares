package dev.tigr.ares.forge.impl.modules.combat;

import dev.tigr.ares.core.feature.module.Category;
import dev.tigr.ares.core.feature.module.Module;
import dev.tigr.ares.core.setting.Setting;
import dev.tigr.ares.core.setting.settings.BooleanSetting;
import dev.tigr.ares.core.util.render.TextColor;
import dev.tigr.ares.forge.impl.modules.exploit.InstantMine;
import dev.tigr.ares.forge.utils.Comparators;
import dev.tigr.ares.forge.utils.MathUtils;
import dev.tigr.ares.forge.utils.WorldUtils;
import dev.tigr.ares.forge.utils.entity.SelfUtils;
import net.minecraft.block.BlockAir;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemPickaxe;
import net.minecraft.network.play.client.CPacketAnimation;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static dev.tigr.ares.forge.impl.modules.player.PacketMine.MINER;

/**
 * @author Tigermouthbear 12/12/20
 */
@Module.Info(name = "AutoCity", description = "Automatically mines closest players surround", category = Category.COMBAT)
public class AutoCity extends Module {
    //TODO: fix skip queue
    private final Setting<Boolean> instant = register(new BooleanSetting("Instant", true));
    private final Setting<Boolean> burrow = register(new BooleanSetting("Mine Burrow First", true));
    private final Setting<Boolean> oneDotThirteen = register(new BooleanSetting("1.13+", false));
//    public final Setting<Boolean> skipQueue = register(new BooleanSetting("Skip Mine Queue", false)).setVisibility(MINER.queue::getValue);

    private boolean toggleInstant = false;
    
    @Override
    public void onEnable() {
        if (instant.getValue() && !InstantMine.INSTANCE.getEnabled()) {
            toggleInstant = true;
            InstantMine.INSTANCE.setEnabled(true);
        }
        // get targets
        for(EntityPlayer playerEntity: WorldUtils.getPlayerTargets(MC.playerController.getBlockReachDistance() +2)) {
            Vec3d posVec = playerEntity.getPositionVector();
            BlockPos pos = new BlockPos(Math.floor(posVec.x), Math.round(posVec.y), Math.floor(posVec.z));
            if(inCity(pos) || (burrow.getValue() && inBurrow(pos))) {
                // find block
                List<BlockPos> blocks = new ArrayList<>(Arrays.asList(pos.north(), pos.east(), pos.south(), pos.west()));
                blocks.sort(Comparators.blockDistance);
                if(burrow.getValue()) blocks.add(0, pos);

                BlockPos target = null;
                for(BlockPos block: blocks) {
                    Vec3d closest = MathUtils.getClosestPointOfBlockPos(SelfUtils.getEyePos(), block);
                    if(!inPlayerCity(block) && MC.world.getBlockState(block).getBlock() != Blocks.BEDROCK && MathUtils.squaredDistanceBetween(SelfUtils.getEyePos(), closest) <= MC.playerController.getBlockReachDistance() * MC.playerController.getBlockReachDistance()) {
                        if(shouldBreakCheck(block, pos)) {
                            target = block;
                            break;
                        }
                    }
                }
                if(target == null) continue;

                // check player has a pick
                int index = -1;
                for(int i = 0; i < 9; i++) {
                    if(MC.player.inventory.getStackInSlot(i).getItem() instanceof ItemPickaxe) {
                        index = i;
                        break;
                    }
                }
                if(index == -1) UTILS.printMessage("No pickaxe in hotbar!");
                else {
                    // break
                    if(instant.getValue()) {
                        MC.player.inventory.currentItem = index;
                        MC.playerController.onPlayerDamageBlock(target, EnumFacing.UP);
                        MC.player.connection.sendPacket(new CPacketAnimation(EnumHand.MAIN_HAND));
                    } else {
//                        if(skipQueue.getValue() && MINER.queue.getValue()) MINER.setTarget(target);
//                        else
                        MINER.addPos(target);
                    }
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

    private boolean inBurrow(BlockPos pos) {
        return allBlocks(pos);
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
        if(burrow.getValue() && pos.equals(target))
            return !MC.world.getBlockState(pos).getMaterial().isReplaceable();
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
