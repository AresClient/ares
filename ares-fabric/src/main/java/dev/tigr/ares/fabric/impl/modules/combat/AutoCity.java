package dev.tigr.ares.fabric.impl.modules.combat;

import dev.tigr.ares.core.feature.FriendManager;
import dev.tigr.ares.core.feature.module.Category;
import dev.tigr.ares.core.feature.module.Module;
import dev.tigr.ares.core.setting.Setting;
import dev.tigr.ares.core.setting.settings.BooleanSetting;
import dev.tigr.ares.core.setting.settings.numerical.DoubleSetting;
import dev.tigr.ares.core.util.render.TextColor;
import dev.tigr.ares.fabric.impl.modules.exploit.InstantMine;
import dev.tigr.ares.fabric.utils.Comparators;
import dev.tigr.ares.fabric.utils.WorldUtils;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.PickaxeItem;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

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
    private final Setting<Boolean> oneDotThirteen = register(new BooleanSetting("1.13+", true));

    private boolean toggleInstant = false;

    @Override
    public void onEnable() {
        if (instant.getValue() && !InstantMine.INSTANCE.getEnabled()) {
            toggleInstant = true;
            InstantMine.INSTANCE.setEnabled(true);
        }
        // get targets
        List<PlayerEntity> targets = MC.world.getPlayers().stream().filter(entityPlayer -> !FriendManager.isFriend(entityPlayer.getGameProfile().getName()) && entityPlayer != MC.player).collect(Collectors.toList());
        targets.sort(Comparators.entityDistance);
        
        for(PlayerEntity playerEntity: targets) {
            BlockPos pos = playerEntity.getBlockPos();
            if(inCity(pos)) {
                // find block
                List<BlockPos> blocks = Arrays.asList(pos.north(), pos.east(), pos.south(), pos.west());
                blocks.sort(Comparators.blockDistance);
                BlockPos target = null;
                for(BlockPos block: blocks) {
                    if(!inPlayerCity(block) && MC.world.getBlockState(block).getBlock() != Blocks.BEDROCK && MC.player.squaredDistanceTo(block.getX() + 0.5, block.getY() + 0.5, block.getZ() + 0.5) < range.getValue() * range.getValue()) {
                        if (oneDotThirteen.getValue() || MC.world.getBlockState(new BlockPos(block.getX(), block.getY() + 1, block.getZ())).getBlock() == Blocks.AIR) {
                            target = block;
                            break;
                        }
                    }
                }
                if(target == null) continue;

                // find pick
                int index = -1;
                for(int i = 0; i < 9; i++) {
                    if(MC.player.inventory.getStack(i).getItem() instanceof PickaxeItem) {
                        index = i;
                        break;
                    }
                }
                if(index == -1) UTILS.printMessage("No pickaxe in hotbar!");
                else {
                    // switch to pick
                    MC.player.inventory.selectedSlot = index;
                    MC.player.networkHandler.sendPacket(new UpdateSelectedSlotC2SPacket(index));

                    // rotate
                    if (rotate.getValue()) {
                        double[] rotations = WorldUtils.calculateLookAt(target.getX() + 0.5, target.getY() + 0.5, target.getZ() + 0.5, MC.player);
                        MC.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.LookOnly((float) rotations[0], (float) rotations[1], MC.player.isOnGround()));
                    }

                    // break
                    MC.player.networkHandler.sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.START_DESTROY_BLOCK, target, Direction.UP));
                    MC.player.swingHand(Hand.MAIN_HAND);
                    if (instant.getValue()) MC.interactionManager.attackBlock(target, Direction.UP);
                    MC.player.networkHandler.sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.STOP_DESTROY_BLOCK, target, Direction.UP));
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
}
