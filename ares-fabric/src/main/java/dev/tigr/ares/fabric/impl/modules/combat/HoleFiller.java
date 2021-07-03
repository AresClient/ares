package dev.tigr.ares.fabric.impl.modules.combat;

import dev.tigr.ares.core.feature.FriendManager;
import dev.tigr.ares.core.feature.module.Category;
import dev.tigr.ares.core.feature.module.Module;
import dev.tigr.ares.core.setting.Setting;
import dev.tigr.ares.core.setting.settings.BooleanSetting;
import dev.tigr.ares.core.setting.settings.numerical.DoubleSetting;
import dev.tigr.ares.fabric.utils.InventoryUtils;
import dev.tigr.ares.fabric.utils.WorldUtils;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;

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

    @Override
    public void onTick() {
        Optional<AbstractClientPlayerEntity> players = MC.world.getPlayers().stream().filter(player -> player != MC.player && !FriendManager.isFriend(player.getGameProfile().getName())).min(Comparator.comparing(player -> player.distanceTo(MC.player)));
        if(!players.isPresent()) return;

        List<BlockPos> holes = WorldUtils.getBlocksInReachDistance().stream().filter(pos -> isHole(pos) && MC.player.squaredDistanceTo(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5) < range.getValue() * range.getValue()).collect(Collectors.toList());

        BlockPos nearest = null;
        for(BlockPos hole: holes) {
            if(nearest == null) nearest = hole;
            if(MC.player.squaredDistanceTo(hole.getX() + 0.5, hole.getY() + 0.5, hole.getZ() + 0.5) < MC.player.squaredDistanceTo(nearest.getX() + 0.5, nearest.getY() + 0.5, nearest.getZ() + 0.5)) nearest = hole;
        }
        if(nearest == null) return;

        for(BlockPos hole: holes) {
            if(hole == nearest && skipNearby.getValue()) continue;

            if(
                    MC.world.getOtherEntities(
                            null,
                            new Box(hole)
                    ).stream().noneMatch(Entity::collides)
            ) {
                int first = MC.player.getInventory().selectedSlot;
                int slot = InventoryUtils.findBlockInHotbar(Blocks.OBSIDIAN);
                if(slot == -1) {
                    slot = InventoryUtils.findBlockInHotbar(Blocks.CRYING_OBSIDIAN);
                    if(slot == -1) return;
                }
                MC.player.getInventory().selectedSlot = slot;

                WorldUtils.placeBlockMainHand(hole, rotate.getValue());
                MC.player.getInventory().selectedSlot = first;
                return;
            }
        }
    }

    private boolean isHole(BlockPos pos) {
        BlockState[] blockStates = new BlockState[] {
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
