package dev.tigr.ares.forge.impl.modules.combat;

import dev.tigr.ares.core.feature.FriendManager;
import dev.tigr.ares.core.feature.module.Category;
import dev.tigr.ares.core.feature.module.Module;
import dev.tigr.ares.core.setting.Setting;
import dev.tigr.ares.core.setting.settings.EnumSetting;
import dev.tigr.ares.core.setting.settings.numerical.DoubleSetting;
import dev.tigr.ares.core.setting.settings.numerical.IntegerSetting;
import dev.tigr.ares.forge.utils.InventoryUtils;
import dev.tigr.ares.forge.utils.WorldUtils;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;

/**
 * @author Tigermouthbear
 */
@Module.Info(name = "AutoTrap", description = "Automatically trap people in holes", category = Category.COMBAT)
public class AutoTrap extends Module {
    private final Setting<Mode> mode = register(new EnumSetting<>("Mode", Mode.FULL));
    private final Setting<Double> range = register(new DoubleSetting("Range", 8.0D, 0.0D, 15.0D));
    private final Setting<Integer> delay = register(new IntegerSetting("Delay", 2, 0, 10));

    @Override
    public void onTick() {
        if(MC.player.ticksExisted % delay.getValue() != 0) return;

        for(EntityPlayer player: MC.world.playerEntities) {
            if(FriendManager.isFriend(player.getGameProfile().getName()) || MC.player == player) continue;

            if(MC.player.getDistance(player) <= range.getValue()) {
                for(BlockPos pos: getPos(player)) {
                    if(MC.world.getBlockState(pos).getMaterial().isReplaceable()) {
                        if(
                                MC.world.getEntitiesWithinAABBExcludingEntity(
                                        null,
                                        new AxisAlignedBB(pos)
                                ).isEmpty()
                        ) {
                            //place block
                            int oldSlot = MC.player.inventory.currentItem;
                            int newSlot = InventoryUtils.findBlockInHotbar(Blocks.OBSIDIAN);
                            if(newSlot == -1) return;
                            else MC.player.inventory.currentItem = newSlot;
                            WorldUtils.placeBlockMainHand(pos);
                            MC.player.inventory.currentItem = oldSlot;
                            return;
                        }
                    }
                }
            }
        }
    }

    private BlockPos[] getPos(EntityPlayer player) {
        BlockPos playerPos = new BlockPos(player.getPositionVector());
        BlockPos[] blocks;

        if(mode.getValue() == Mode.FULL) {
            blocks = new BlockPos[]{
                    playerPos.add(1, -1, 0),
                    playerPos.add(1, 0, 0),
                    playerPos.add(1, 1, 0),

                    playerPos.add(-1, -1, 0),
                    playerPos.add(-1, 0, 0),
                    playerPos.add(-1, 1, 0),

                    playerPos.add(0, -1, 1),
                    playerPos.add(0, 0, 1),
                    playerPos.add(0, 1, 1),

                    playerPos.add(0, -1, -1),
                    playerPos.add(0, 0, -1),
                    playerPos.add(0, 1, -1),

                    playerPos.add(1, 2, 0),
                    playerPos.add(0, 2, 0)
            };
        } else {
            blocks = new BlockPos[]{
                    playerPos.add(1, -1, 1),
                    playerPos.add(1, 0, 1),
                    playerPos.add(1, 1, 1),

                    playerPos.add(1, -1, -1),
                    playerPos.add(1, 0, -1),
                    playerPos.add(1, 1, -1),

                    playerPos.add(-1, -1, 1),
                    playerPos.add(-1, 0, 1),
                    playerPos.add(-1, 1, 1),

                    playerPos.add(-1, -1, -1),
                    playerPos.add(-1, 0, -1),
                    playerPos.add(-1, 1, -1),

                    playerPos.add(0, 1, 1),
                    playerPos.add(0, 1, -1),
                    playerPos.add(1, 1, 0),
                    playerPos.add(-1, 1, 0),

                    playerPos.add(1, 2, 0),
                    playerPos.add(0, 2, 0)
            };
        }

        return blocks;
    }

    enum Mode {FULL, CRYSTAL}
}
