package dev.tigr.ares.fabric.impl.modules.combat;

import dev.tigr.ares.core.feature.module.Category;
import dev.tigr.ares.core.feature.module.Module;
import dev.tigr.ares.core.setting.Setting;
import dev.tigr.ares.core.setting.settings.BooleanSetting;
import dev.tigr.ares.core.setting.settings.EnumSetting;
import dev.tigr.ares.core.setting.settings.numerical.DoubleSetting;
import dev.tigr.ares.core.setting.settings.numerical.IntegerSetting;
import dev.tigr.ares.core.util.Priorities;
import dev.tigr.ares.core.util.Timer;
import dev.tigr.ares.fabric.utils.InventoryUtils;
import dev.tigr.ares.fabric.utils.entity.PlayerUtils;
import dev.tigr.ares.fabric.utils.entity.SelfUtils;
import net.minecraft.block.Blocks;
import net.minecraft.block.ShapeContext;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

import static dev.tigr.ares.fabric.impl.modules.player.RotationManager.ROTATIONS;

/**
 * @author Tigermouthbear
 */
@Module.Info(name = "AutoTrap", description = "Automatically trap people in holes", category = Category.COMBAT)
public class AutoTrap extends Module {
    private final Setting<Mode> mode = register(new EnumSetting<>("Mode", Mode.FULL));
    private final Setting<Boolean> rotate = register(new BooleanSetting("Rotate", true));
    private final Setting<Double> range = register(new DoubleSetting("Range", 5, 0, 8));
    private final Setting<Integer> delay = register(new IntegerSetting("Delay", 2, 0, 10));
    private final Setting<Integer> blocksPerTick = register(new IntegerSetting("Blocks Per Tick", 8, 0, 20)).setVisibility(() -> delay.getValue() == 0);

    private final Timer delayTimer = new Timer();

    int key = Priorities.Rotation.AUTO_TRAP;

    int blocksPlaced = 0;

    @Override
    public void onDisable() {
        if(rotate.getValue()) ROTATIONS.setCompletedAction(key, true);
    }

    @Override
    public void onTick() {
        //Flag rotations off if there are no placements needing completion
        boolean flagCurrent = true;
        for(PlayerEntity player: SelfUtils.getPlayersInRadius(range.getValue()))
            if(isPlayerValidTarget(player))
                for(BlockPos pos: getPos(player))
                    if(isPosInRange(pos)
                            && MC.world.getBlockState(pos).getMaterial().isReplaceable()
                            && MC.world.canPlace(Blocks.OBSIDIAN.getDefaultState(), pos, ShapeContext.absent()))
                        flagCurrent = false;

        if(ROTATIONS.isKeyCurrent(key) && flagCurrent) ROTATIONS.setCompletedAction(key, true);

        int oldSlot = MC.player.getInventory().selectedSlot;

        if(delayTimer.passedTicks(delay.getValue())) {
            for(PlayerEntity player: SelfUtils.getPlayersInRadius(range.getValue())) {
                if(isPlayerValidTarget(player)) {

                    int newSlot = InventoryUtils.findBlockInHotbar(Blocks.OBSIDIAN);
                    if(delay.getValue() == 0) {
                        if(newSlot == -1) return;
                        else MC.player.getInventory().selectedSlot = newSlot;
                    }
                    for(BlockPos pos: getPos(player)) {
                        if(MC.world.getBlockState(pos).getMaterial().isReplaceable() && isPosInRange(pos)) {
                            if(MC.world.getOtherEntities(null, new Box(pos)).isEmpty()) {
                                //place block
                                if(delay.getValue() != 0) {
                                    if(newSlot == -1) return;
                                    else MC.player.getInventory().selectedSlot = newSlot;
                                }

                                SelfUtils.placeBlockMainHand(rotate.getValue(), key, key, delay.getValue() == 0, false, pos);

                                if(delay.getValue() != 0) MC.player.getInventory().selectedSlot = oldSlot;

                                delayTimer.reset();

                                if(delay.getValue() == 0) {
                                    blocksPlaced++;
                                    if(blocksPlaced < blocksPerTick.getValue()) continue;
                                    else {
                                        MC.player.getInventory().selectedSlot = oldSlot;
                                        blocksPlaced = 0;
                                        return;
                                    }
                                }
                                return;
                            }
                        }
                    }
                }
            }
        }

        MC.player.getInventory().selectedSlot = oldSlot;
    }

    private boolean isPosInRange(BlockPos pos) {
        return MC.player.squaredDistanceTo(Vec3d.ofCenter(pos)) <= range.getValue() * range.getValue();
    }

    private boolean isPlayerValidTarget(PlayerEntity player) {
        return (MC.player.squaredDistanceTo(player) <= range.getValue() * range.getValue()) && PlayerUtils.isValidTarget(player, range.getValue());
    }

    private BlockPos[] getPos(Entity player) {
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
        } else if(mode.getValue() == Mode.CRYSTALAIR) {
            blocks = new BlockPos[]{
                    playerPos.add(0, 1, 1),
                    playerPos.add(0, 1, -1),
                    playerPos.add(1, 1, 0),
                    playerPos.add(-1, 1, 0),

                    playerPos.add(1, 2, 0),
                    playerPos.add(0, 2, 0)
            };
        } else if(mode.getValue() == Mode.CRYSTALTOP) {
            blocks = new BlockPos[]{
                    playerPos.add(1, -1, 0),
                    playerPos.add(1, 0, 0),
                    playerPos.add(1, 1, 0),
                    playerPos.add(1, 2, 0),

                    playerPos.add(0, 2, 0),
                    playerPos.add(-1, 2, 0),
                    playerPos.add(0, 2, 1),
                    playerPos.add(0, 2, -1),

                    playerPos.add(-1, 1, 0),
                    playerPos.add(0, 1, 1),
                    playerPos.add(0, 1, -1),

            };
        } else if(mode.getValue() == Mode.TOPONLY) {
            blocks = new BlockPos[]{
                    playerPos.add(1, -1, 0),
                    playerPos.add(1, 0, 0),
                    playerPos.add(1, 1, 0),
                    playerPos.add(1, 2, 0),

                    playerPos.add(0, 2, 0)
            };
        } else if(mode.getValue() == Mode.TOPAIR) {
            blocks = new BlockPos[]{
                    playerPos.add(0, 2, 0)
            };
        } else if(mode.getValue() == Mode.TOP3x3) {
            blocks = new BlockPos[]{
                    playerPos.add(0, 2, 0),

                    playerPos.add(1, 2, 0),
                    playerPos.add(0, 2, 1),
                    playerPos.add(-1, 2, 0),
                    playerPos.add(0, 2, -1),

                    playerPos.add(1, 2, 1),
                    playerPos.add(1, 2, -1),
                    playerPos.add(-1, 2, -1),
                    playerPos.add(-1, 2, 1)
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


    enum Mode { FULL, CRYSTALAIR, CRYSTALTOP, CRYSTALFULL, TOPONLY, TOPAIR, TOP3x3 }
}
