package dev.tigr.ares.fabric.impl.modules.combat;

import dev.tigr.ares.core.feature.module.Category;
import dev.tigr.ares.core.feature.module.Module;
import dev.tigr.ares.core.setting.Setting;
import dev.tigr.ares.core.setting.settings.BooleanSetting;
import dev.tigr.ares.core.setting.settings.EnumSetting;
import dev.tigr.ares.core.setting.settings.numerical.IntegerSetting;
import dev.tigr.ares.core.util.Priorities;
import dev.tigr.ares.core.util.Timer;
import dev.tigr.ares.fabric.utils.InventoryUtils;
import dev.tigr.ares.fabric.utils.entity.SelfUtils;
import net.minecraft.block.Blocks;
import net.minecraft.block.ShapeContext;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;

import java.util.ArrayList;
import java.util.List;

import static dev.tigr.ares.fabric.impl.modules.player.RotationManager.ROTATIONS;

/**
 * @author Makrennel
 */
@Module.Info(name = "SelfTrap", description = "Places a trap around yourself", category = Category.COMBAT)
public class SelfTrap extends Module {
    private final Setting<Mode> mode = register(new EnumSetting<>("Mode", Mode.FULL));
    private final Setting<Boolean> cev = register(new BooleanSetting("Cev", true));
    private final Setting<Boolean> snap = register(new BooleanSetting("Snap", true));
    private final Setting<Boolean> rotate = register(new BooleanSetting("Rotate", true));
    private final Setting<Integer> delay = register(new IntegerSetting("Delay", 2, 0, 10));
    private final Setting<Integer> blocksPerTick = register(new IntegerSetting("Blocks Per Tick", 2, 0, 20)).setVisibility(() -> delay.getValue() == 0);

    private final Timer delayTimer = new Timer();

    int key = Priorities.Rotation.SELF_TRAP;

    int blocksPlaced = 0;

    boolean hasSnapped = false;

    @Override
    public void onDisable() {
        if(rotate.getValue()) ROTATIONS.setCompletedAction(key, true);
        hasSnapped = false;
    }

    @Override
    public void onTick() {
        //Center
        if(snap.getValue() && !hasSnapped && MC.player.isOnGround()) {
            SelfUtils.snapPlayer(SelfUtils.getBlockPosCorrected());
            hasSnapped = true;
        }

        //Flag rotations off if there are no placements needing completion
        boolean flagCurrent = true;
        for(BlockPos pos: getPos())
            if(MC.world.getBlockState(pos).getMaterial().isReplaceable() && MC.world.canPlace(Blocks.OBSIDIAN.getDefaultState(), pos, ShapeContext.absent()))
                flagCurrent = false;

        if(ROTATIONS.isKeyCurrent(key) && flagCurrent) ROTATIONS.setCompletedAction(key, true);

        int oldSlot = MC.player.inventory.selectedSlot;

        if(delayTimer.passedMillis(delay.getValue())) {
            int newSlot = InventoryUtils.findBlockInHotbar(Blocks.OBSIDIAN);
            if(delay.getValue() == 0) {
                if(newSlot == -1) return;
                else MC.player.inventory.selectedSlot = newSlot;
            }
            for(BlockPos pos: getPos()) {
                if(MC.world.getBlockState(pos).getMaterial().isReplaceable()) {
                    if(MC.world.getOtherEntities(null, new Box(pos)).isEmpty()) {
                        //place block
                        if(delay.getValue() != 0) {
                            if(newSlot == -1) return;
                            else MC.player.inventory.selectedSlot = newSlot;
                        }

                        SelfUtils.placeBlockMainHand(rotate.getValue(), key, key, delay.getValue() == 0, false, pos);

                        if(delay.getValue() != 0) MC.player.inventory.selectedSlot = oldSlot;

                        delayTimer.reset();

                        if(delay.getValue() == 0) {
                            blocksPlaced++;
                            if(blocksPlaced < blocksPerTick.getValue()) continue;
                            else {
                                MC.player.inventory.selectedSlot = oldSlot;
                                blocksPlaced = 0;
                                return;
                            }
                        }
                        return;
                    }
                }
            }
        }

        MC.player.inventory.selectedSlot = oldSlot;
    }

    private List<BlockPos> getPos() {
        BlockPos playerPos = SelfUtils.getBlockPosCorrected();
        List<BlockPos> blocks = new ArrayList<>();

        if(mode.getValue() == Mode.FULL) {
            blocks.add(playerPos.add(1, -1, 0));
            blocks.add(playerPos.add(1, 0, 0));
            blocks.add(playerPos.add(1, 1, 0));

            blocks.add(playerPos.add(-1, -1, 0));
            blocks.add(playerPos.add(-1, 0, 0));
            blocks.add(playerPos.add(-1, 1, 0));

            blocks.add(playerPos.add(0, -1, 1));
            blocks.add(playerPos.add(0, 0, 1));
            blocks.add(playerPos.add(0, 1, 1));

            blocks.add(playerPos.add(0, -1, -1));
            blocks.add(playerPos.add(0, 0, -1));
            blocks.add(playerPos.add(0, 1, -1));

            blocks.add(playerPos.add(1, 2, 0));
            blocks.add(playerPos.add(0, 2, 0));
        } else if(mode.getValue() == Mode.TOPONLY) {
            blocks.add(playerPos.add(1, -1, 0));
            blocks.add(playerPos.add(1, 0, 0));
            blocks.add(playerPos.add(1, 1, 0));
            blocks.add(playerPos.add(1, 2, 0));

            blocks.add(playerPos.add(0, 2, 0));
        } else if(mode.getValue() == Mode.TOPAIR) {
            blocks.add(playerPos.add(0, 2, 0));
        } else if(mode.getValue() == Mode.TOP3x3) {
            blocks.add(playerPos.add(0, 2, 0));

            blocks.add(playerPos.add(1, 2, 0));
            blocks.add(playerPos.add(0, 2, 1));
            blocks.add(playerPos.add(-1, 2, 0));
            blocks.add(playerPos.add(0, 2, -1));

            blocks.add(playerPos.add(1, 2, 1));
            blocks.add(playerPos.add(1, 2, -1));
            blocks.add(playerPos.add(-1, 2, -1));
            blocks.add(playerPos.add(-1, 2, 1));
        }

        if(cev.getValue()) blocks.add(playerPos.add(0, 3, 0));

        return blocks;
    }


    enum Mode { FULL, TOPONLY, TOPAIR, TOP3x3 }
}
