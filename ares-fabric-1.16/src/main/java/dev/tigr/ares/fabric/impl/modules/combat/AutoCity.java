package dev.tigr.ares.fabric.impl.modules.combat;

import dev.tigr.ares.core.feature.module.Category;
import dev.tigr.ares.core.feature.module.Module;
import dev.tigr.ares.core.setting.Setting;
import dev.tigr.ares.core.setting.settings.BooleanSetting;
import dev.tigr.ares.core.util.render.TextColor;
import dev.tigr.ares.fabric.impl.modules.exploit.InstantMine;
import dev.tigr.ares.fabric.utils.Comparators;
import dev.tigr.ares.fabric.utils.MathUtils;
import dev.tigr.ares.fabric.utils.WorldUtils;
import dev.tigr.ares.fabric.utils.entity.SelfUtils;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.item.PickaxeItem;
import net.minecraft.network.packet.c2s.play.HandSwingC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

import java.util.Arrays;
import java.util.List;

import static dev.tigr.ares.fabric.impl.modules.player.PacketMine.MINER;

/**
 * @author Tigermouthbear 12/12/20
 */
@Module.Info(name = "AutoCity", description = "Automatically mines closest players surround", category = Category.COMBAT)
public class AutoCity extends Module {
    private final Setting<Boolean> instant = register(new BooleanSetting("Instant", true));
    private final Setting<Boolean> oneDotThirteen = register(new BooleanSetting("1.13+", true));
    public final Setting<Boolean> skipQueue = register(new BooleanSetting("Skip Mine Queue", false));

    private boolean toggleInstant = false;

    @Override
    public void onEnable() {
        if (instant.getValue() && !InstantMine.INSTANCE.getEnabled()) {
            toggleInstant = true;
            InstantMine.INSTANCE.setEnabled(true);
        }
        // get targets
        for(Entity playerEntity: WorldUtils.getPlayerTargets(MC.interactionManager.getReachDistance() +2)) {
            Vec3d posVec = playerEntity.getPos();
            BlockPos pos = new BlockPos(Math.floor(posVec.x), Math.floor(posVec.y), Math.floor(posVec.z));
            if(inCity(pos)) {
                // find block
                List<BlockPos> blocks = Arrays.asList(pos.north(), pos.east(), pos.south(), pos.west());
                blocks.sort(Comparators.blockDistance);
                BlockPos target = null;
                for(BlockPos block: blocks) {
                    Vec3d closest = MathUtils.getClosestPointOfBlockPos(SelfUtils.getEyePos(), block);
                    if(!inPlayerCity(block) && MC.world.getBlockState(block).getBlock() != Blocks.BEDROCK && MathUtils.squaredDistanceBetween(SelfUtils.getEyePos(), closest) <= MC.interactionManager.getReachDistance() * MC.interactionManager.getReachDistance()) {
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
                    if(MC.player.inventory.getStack(i).getItem() instanceof PickaxeItem) {
                        index = i;
                        break;
                    }
                }
                if(index == -1) UTILS.printMessage("No pickaxe in hotbar!");
                else {
                    // break
                    if(instant.getValue()) {
                        MC.player.inventory.selectedSlot = index;
                        MC.interactionManager.updateBlockBreakingProgress(target, Direction.UP);
                        MC.player.networkHandler.sendPacket(new HandSwingC2SPacket(Hand.MAIN_HAND));
                    } else {
                        if(MINER.queue.getValue() && skipQueue.getValue()) MINER.setTarget(target);
                        else MINER.addPos(target);
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
    
    private boolean inCity(BlockPos pos) {
        return allBlocks(pos.north(), pos.east(), pos.south(), pos.west());
    }

    private boolean inPlayerCity(BlockPos pos) {
        BlockPos current = MC.player.getBlockPos();
        return pos.north() == current || pos.east() == current || pos.south() == current || pos.west() == current;
    }
    
    private boolean allBlocks(BlockPos... pos) {
        return Arrays.stream(pos).allMatch(blockPos -> !MC.world.getBlockState(blockPos).isAir());
    }

    private boolean shouldBreakCheck(BlockPos pos, BlockPos target) {
        if(oneDotThirteen.getValue()) return true;
        else if(MC.world.getBlockState(pos.up()).isAir()) return true;
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
        return MC.world.getBlockState(pos).isAir() && MC.world.getBlockState(pos.up()).isAir();
    }
}
