package dev.tigr.ares.forge.impl.modules.combat;

import dev.tigr.ares.core.feature.module.Category;
import dev.tigr.ares.core.feature.module.Module;
import dev.tigr.ares.core.setting.Setting;
import dev.tigr.ares.core.setting.settings.BooleanSetting;
import dev.tigr.ares.core.setting.settings.numerical.IntegerSetting;
import dev.tigr.ares.forge.impl.modules.player.Freecam;
import dev.tigr.ares.forge.utils.InventoryUtils;
import dev.tigr.ares.forge.utils.WorldUtils;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.util.math.BlockPos;

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
    private final Setting<Integer> delay = register(new IntegerSetting("Delay", 0, 0, 10));
    private final Setting<Boolean> rotate = register(new BooleanSetting("Rotate", true));
    private final Setting<Boolean> air = register(new BooleanSetting("Air-place", false));
    private BlockPos lastPos = new BlockPos(0, -100, 0);
    private int ticks = 0;

    public Surround() {
        INSTANCE = this;
    }

    // this is to allow turning off Center when toggling surround from another module without the player having to disable Center in Surround itself
    boolean doSnap = true;
    public static void toggleCenter(boolean doSnap) {
        INSTANCE.doSnap = doSnap;
    }

    @Override
    public void onTick() {
        if(!MC.player.onGround || (delay.getValue() != 0 && ticks++ % delay.getValue() != 0)) return;

        // make sure player is in the same place
        AbstractClientPlayer loc = Freecam.INSTANCE.getEnabled() ? Freecam.INSTANCE.clone : MC.player;
        if(!lastPos.equals(WorldUtils.roundBlockPos(loc.getPositionVector()))) {
            setEnabled(false);
            return;
        }

        // find obby
        int obbyIndex = InventoryUtils.findBlockInHotbar(Blocks.OBSIDIAN);
        if(obbyIndex == -1) return;
        int prevSlot = MC.player.inventory.currentItem;

        if(needsToPlace()) {
            for(BlockPos pos: getPositions()) {
                MC.player.inventory.currentItem = obbyIndex;
                if(WorldUtils.placeBlockMainHand(pos, rotate.getValue()) && delay.getValue() != 0) return;
            }

            MC.player.inventory.currentItem = prevSlot;
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
                MC.world.getBlockState(pos).getBlock() == Blocks.AIR &&
                allAir(pos.north(), pos.east(), pos.south(), pos.west(), pos.up(), pos.down()) &&
                !air.getValue()
        ) list.add(pos.down());
        list.add(pos);
    }

    private boolean allAir(BlockPos... pos) {
        return Arrays.stream(pos).allMatch(blockPos -> MC.world.getBlockState(blockPos).getBlock() == Blocks.AIR);
    }

    private boolean anyAir(BlockPos... pos) {
        return Arrays.stream(pos).anyMatch(blockPos -> MC.world.getBlockState(blockPos).getBlock() == Blocks.AIR);
    }

    @Override
    public void onEnable() {
        lastPos = WorldUtils.roundBlockPos(MC.player.getPositionVector());

        if(snap.getValue() && doSnap) {
            double xPos = MC.player.getPositionVector().x;
            double zPos = MC.player.getPositionVector().z;

            if(Math.abs((lastPos.getX() + 0.5) - MC.player.getPositionVector().x) >= 0.2) {
                int xDir = (lastPos.getX() + 0.5) - MC.player.getPositionVector().x > 0 ? 1 : -1;
                xPos += 0.3 * xDir;
            }

            if(Math.abs((lastPos.getZ() + 0.5) - MC.player.getPositionVector().z) >= 0.2) {
                int zDir = (lastPos.getZ() + 0.5) - MC.player.getPositionVector().z > 0 ? 1 : -1;
                zPos += 0.3 * zDir;
            }

            MC.player.motionX = MC.player.motionY = MC.player.motionZ = 0;
            MC.player.setPosition(xPos, MC.player.posY, zPos);
            MC.player.connection.sendPacket(new CPacketPlayer.Position(xPos, MC.player.posY, zPos, MC.player.onGround));
        }
    }

    @Override
    public void onDisable() {
        ticks = 0;
        doSnap = true;
    }
}
