package dev.tigr.ares.forge.impl.modules.combat;

import dev.tigr.ares.core.feature.FriendManager;
import dev.tigr.ares.core.feature.module.Category;
import dev.tigr.ares.core.feature.module.Module;
import dev.tigr.ares.core.setting.Setting;
import dev.tigr.ares.core.setting.settings.numerical.DoubleSetting;
import dev.tigr.ares.core.util.render.TextColor;
import dev.tigr.ares.forge.utils.Comparators;
import dev.tigr.ares.forge.utils.WorldUtils;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemPickaxe;
import net.minecraft.network.play.client.CPacketHeldItemChange;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.network.play.client.CPacketPlayerDigging;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Tigermouthbear 12/12/20
 */
@Module.Info(name = "AutoCity", description = "Automatically mines closest players surround", category = Category.COMBAT)
public class AutoCity extends Module {
    private final Setting<Double> range = register(new DoubleSetting("Range", 5, 0, 10));
    
    @Override
    public void onEnable() {
        // get targets
        List<EntityPlayer> targets = MC.world.playerEntities.stream().filter(entityPlayer -> !FriendManager.isFriend(entityPlayer.getGameProfile().getName()) && entityPlayer != MC.player).collect(Collectors.toList());
        targets.sort(Comparators.entityDistance);
        
        for(EntityPlayer playerEntity: targets) {
            BlockPos pos = playerEntity.getPosition();
            if(inCity(pos)) {
                // find block
                List<BlockPos> blocks = Arrays.asList(pos.north(), pos.east(), pos.south(), pos.west());
                blocks.sort(Comparators.blockDistance);
                BlockPos target = null;
                for(BlockPos block: blocks) {
                    if(!inPlayerCity(block) && MC.world.getBlockState(block).getBlock() != Blocks.BEDROCK && MC.player.getDistanceSq(block.getX() + 0.5, block.getY() + 0.5, block.getZ() + 0.5) < range.getValue() * range.getValue()) {
                        target = block;
                        break;
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
                    double[] rotations = WorldUtils.calculateLookAt(target.getX() + 0.5, target.getY() + 0.5, target.getZ() + 0.5, MC.player);
                    MC.player.connection.sendPacket(new CPacketPlayer.Rotation((float) rotations[0], (float) rotations[1], MC.player.onGround));

                    // break
                    MC.player.connection.sendPacket(new CPacketPlayerDigging(CPacketPlayerDigging.Action.START_DESTROY_BLOCK, target, EnumFacing.UP));
                    MC.player.connection.sendPacket(new CPacketPlayerDigging(CPacketPlayerDigging.Action.STOP_DESTROY_BLOCK, target, EnumFacing.UP));
                }
                setEnabled(false);
                return;
            }
        }
        UTILS.printMessage(TextColor.RED + "Could not find a target!");
        setEnabled(false);
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
}
