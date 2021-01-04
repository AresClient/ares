package dev.tigr.ares.fabric.impl.modules.combat;

import dev.tigr.ares.core.feature.FriendManager;
import dev.tigr.ares.core.feature.module.Category;
import dev.tigr.ares.core.feature.module.Module;
import dev.tigr.ares.core.setting.Setting;
import dev.tigr.ares.core.setting.settings.EnumSetting;
import dev.tigr.ares.core.setting.settings.numerical.DoubleSetting;
import dev.tigr.ares.core.setting.settings.numerical.IntegerSetting;
import dev.tigr.ares.fabric.utils.InventoryUtils;
import dev.tigr.ares.fabric.utils.WorldUtils;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;

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
        if(TICKS % delay.getValue() != 0) return;

        for(PlayerEntity player: MC.world.getPlayers()) {
            if(FriendManager.isFriend(player.getGameProfile().getName()) || MC.player == player) continue;

            if(MC.player.distanceTo(player) <= range.getValue()) {
                for(BlockPos pos: getPos(player)) {
                    if(MC.world.getBlockState(pos).getMaterial().isReplaceable()) {
                        if(
                                MC.world.getOtherEntities(
                                        null,
                                        new Box(pos)
                                ).isEmpty()
                        ) {
                            //place block
                            int oldSlot = MC.player.inventory.selectedSlot;
                            int newSlot = InventoryUtils.findBlock(Blocks.OBSIDIAN);
                            if(newSlot == -1) return;
                            else MC.player.inventory.selectedSlot = newSlot;
                            WorldUtils.placeBlockMainHand(pos);
                            MC.player.inventory.selectedSlot = oldSlot;
                            return;
                        }
                    }
                }
            }
        }
    }

    private BlockPos[] getPos(PlayerEntity player) {
        BlockPos playerPos = new BlockPos(player.getPos());
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
