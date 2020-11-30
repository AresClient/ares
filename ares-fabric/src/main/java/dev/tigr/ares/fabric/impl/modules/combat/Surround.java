package dev.tigr.ares.fabric.impl.modules.combat;

import dev.tigr.ares.core.feature.module.Category;
import dev.tigr.ares.core.feature.module.Module;
import dev.tigr.ares.core.setting.Setting;
import dev.tigr.ares.core.setting.settings.BooleanSetting;
import dev.tigr.ares.fabric.impl.modules.player.Freecam;
import dev.tigr.ares.fabric.utils.InventoryUtils;

import dev.tigr.ares.fabric.utils.WorldUtils;
import net.minecraft.block.Blocks;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.util.math.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author Tigermouthbear
 */
@Module.Info(name = "Surround", description = "Surrounds your feet with obsidian", category = Category.COMBAT)
public class Surround extends Module {
    public static Surround INSTANCE;

    private final Setting<Boolean> snap = register(new BooleanSetting("Center", true));
    private final Setting<Boolean> air = register(new BooleanSetting("Air-place", false));
    private BlockPos lastPos = new BlockPos(0, -100, 0);

    public Surround() {
        INSTANCE = this;
    }

    @Override
    public void onTick() {
        if(!MC.player.isOnGround()) return;

        // make sure player is in the same place
        AbstractClientPlayerEntity loc = Freecam.INSTANCE.getEnabled() ? Freecam.INSTANCE.clone : MC.player;
        if(!loc.getBlockPos().equals(lastPos)) {
            setEnabled(false);
            return;
        }

        // find obby
        int obbyIndex = findBlock();
        if(obbyIndex == -1) return;
        int prevSlot = MC.player.inventory.selectedSlot;

        if(needsToPlace()) {
            for(BlockPos pos: getPositions()) {
                MC.player.inventory.selectedSlot = obbyIndex;
                WorldUtils.placeBlockMainHand(pos);
            }

            MC.player.inventory.selectedSlot = prevSlot;
        }
    }

    private boolean needsToPlace() {
        return anyAir(lastPos.north(), lastPos.east(), lastPos.south(), lastPos.west());
    }

    // returns list of places blocks should be placed at
    private List<BlockPos> getPositions() {
        List<BlockPos> positions = new ArrayList<>();
        add(positions, lastPos.north());
        add(positions, lastPos.east());
        add(positions, lastPos.south());
        add(positions, lastPos.west());
        return positions;
    }

    // adds block to list and structure block if needed to place
    private void add(List<BlockPos> list, BlockPos pos) {
        if(
                MC.world.getBlockState(pos).isAir() &&
                allAir(pos.north(), pos.east(), pos.south(), pos.west(), pos.up(), pos.down()) &&
                !air.getValue()
        ) list.add(pos.down());
        list.add(pos);
    }

    private boolean allAir(BlockPos... pos) {
        return Arrays.stream(pos).allMatch(blockPos -> MC.world.getBlockState(blockPos).isAir());
    }

    private boolean anyAir(BlockPos... pos) {
        return Arrays.stream(pos).anyMatch(blockPos -> MC.world.getBlockState(blockPos).isAir());
    }

    private int findBlock() {
        int index = InventoryUtils.findBlockInHotbar(Blocks.OBSIDIAN);
        return index == -1 ? InventoryUtils.findBlockInHotbar(Blocks.CRYING_OBSIDIAN) : index;
    }

    @Override
    public void onEnable() {
        lastPos = MC.player.getBlockPos();

        if(snap.getValue()) {
            double xPos = MC.player.getPos().x;
            double zPos = MC.player.getPos().z;

            if(Math.abs((lastPos.getX() + 0.5) - MC.player.getPos().x) >= 0.2) {
                int xDir = (lastPos.getX() + 0.5) - MC.player.getPos().x > 0 ? 1 : -1;
                xPos += 0.3 * xDir;
            }

            if(Math.abs((lastPos.getZ() + 0.5) - MC.player.getPos().z) >= 0.2) {
                int zDir = (lastPos.getZ() + 0.5) - MC.player.getPos().z > 0 ? 1 : -1;
                zPos += 0.3 * zDir;
            }

            MC.player.setVelocity(0, 0, 0);
            MC.player.setPos(xPos, lastPos.getY(), zPos);
            MC.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.PositionOnly(xPos, lastPos.getY(), zPos, MC.player.isOnGround()));
        }
    }
}
