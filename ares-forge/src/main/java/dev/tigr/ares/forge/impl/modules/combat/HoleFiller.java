package dev.tigr.ares.forge.impl.modules.combat;

import dev.tigr.ares.core.feature.FriendManager;
import dev.tigr.ares.core.feature.module.Category;
import dev.tigr.ares.core.feature.module.Module;
import dev.tigr.ares.core.setting.Setting;
import dev.tigr.ares.core.setting.settings.BooleanSetting;
import dev.tigr.ares.core.setting.settings.numerical.DoubleSetting;
import dev.tigr.ares.core.util.Priorities;
import dev.tigr.ares.forge.utils.InventoryUtils;
import dev.tigr.ares.forge.utils.WorldUtils;
import dev.tigr.ares.forge.utils.entity.SelfUtils;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author Tigermouthbear
 */
@Module.Info(name = "HoleFiller", description = "Automatically fills nearby holes", category = Category.COMBAT)
public class HoleFiller extends Module {
    private final Setting<Boolean> skipNearby = register(new BooleanSetting("Skip closest", true));
    private final Setting<Boolean> rotate = register(new BooleanSetting("Rotate", true));
    private final Setting<Double> range = register(new DoubleSetting("Range", 5, 0, 10));

    int key = Priorities.Rotation.HOLE_FILLER;

    @Override
    public void onTick() {
        Optional<EntityPlayer> players = MC.world.playerEntities.stream().filter(player -> player != MC.player && !FriendManager.isFriend(player.getGameProfile().getName())).min(Comparator.comparing(player -> player.getDistance(MC.player)));
        if(!players.isPresent()) return;

        List<BlockPos> holes = WorldUtils.getBlocksInReachDistance().stream().filter(pos -> isHole(pos) && MC.player.getDistanceSq(pos) < range.getValue() * range.getValue()).collect(Collectors.toList());

        BlockPos nearest = null;
        for(BlockPos hole: holes) {
            if(nearest == null) nearest = hole;
            if(MC.player.getDistanceSq(hole) < MC.player.getDistanceSq(nearest)) nearest = hole;
        }
        if(nearest == null) return;

        for(BlockPos hole: holes) {
            if(hole == nearest && skipNearby.getValue()) continue;

            if(
                    MC.world.getEntitiesWithinAABBExcludingEntity(
                            null,
                            new AxisAlignedBB(hole)
                    ).stream().noneMatch(Entity::canBeCollidedWith)
            ) {
                int first = MC.player.inventory.currentItem;
                int slot = InventoryUtils.findBlockInHotbar(Blocks.OBSIDIAN);
                if(slot != -1) MC.player.inventory.currentItem = slot;
                else return;

                SelfUtils.placeBlockMainHand(rotate.getValue(), key, key, false, false, hole);
                MC.player.inventory.currentItem = first;
                return;
            }
        }
    }

    private boolean isHole(BlockPos pos) {
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

        return (!blockStates[0].getMaterial().blocksMovement())
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
    }
}
