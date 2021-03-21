package dev.tigr.ares.fabric.impl.modules.combat;

import dev.tigr.ares.core.feature.module.Category;
import dev.tigr.ares.core.feature.module.Module;
import dev.tigr.ares.core.setting.Setting;
import dev.tigr.ares.core.setting.settings.BooleanSetting;
import dev.tigr.ares.core.setting.settings.EnumSetting;
import dev.tigr.ares.core.setting.settings.numerical.IntegerSetting;
import dev.tigr.ares.fabric.impl.modules.player.Freecam;
import dev.tigr.ares.fabric.utils.InventoryUtils;
import dev.tigr.ares.fabric.utils.Timer;
import dev.tigr.ares.fabric.utils.WorldUtils;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
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
    private final Setting<Primary> primary = register(new EnumSetting<>("Main", Primary.Obsidian));
    private final Setting<Boolean> allBlocks = register(new BooleanSetting("All BP Blocks", true));

    enum Primary {Obsidian, EnderChest, CryingObsidian, NetheriteBlock, AncientDebris, EnchantingTable, RespawnAnchor, Anvil}

    private BlockPos lastPos = new BlockPos(0, -100, 0);
    private int ticks = 0;

    public Surround() {
        INSTANCE = this;
    }

    // this is for changing the amount of time it takes to start trying to surround when using other modules that toggle it.
    private static final Timer surroundInstanceDelay = new Timer();
    int timeToStart = 0;
    public static void setSurroundWait(int timeToStart) {
        INSTANCE.timeToStart = timeToStart;
    }

    // this is to allow turning off Center when toggling surround from another module without the player having to disable Center in Surround itself
    boolean doSnap = true;
    public static void toggleCenter(boolean doSnap) {
        INSTANCE.doSnap = doSnap;
    }

    @Override
    public void onTick() {
        if (surroundInstanceDelay.passedMillis(timeToStart)) {
            if (!MC.player.isOnGround() || (delay.getValue() != 0 && ticks++ % delay.getValue() != 0)) return;

            // make sure player is in the same place
            AbstractClientPlayerEntity loc = Freecam.INSTANCE.getEnabled() ? Freecam.INSTANCE.clone : MC.player;
            if (!loc.getBlockPos().equals(lastPos)) {
                setEnabled(false);
                return;
            }

            // find obby
            int obbyIndex = findBlock();
            if (obbyIndex == -1) return;
            int prevSlot = MC.player.inventory.selectedSlot;

            if (needsToPlace()) {
                for (BlockPos pos : getPositions()) {
                    MC.player.inventory.selectedSlot = obbyIndex;
                    if (WorldUtils.placeBlockMainHand(pos, rotate.getValue()) && delay.getValue() != 0) return;
                }

                MC.player.inventory.selectedSlot = prevSlot;
            }
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

    private Block primaryBlock(){
        Block index = null;
        if (primary.getValue() == Primary.Obsidian) {index = Blocks.OBSIDIAN;}
        else if (primary.getValue() == Primary.EnderChest) {index = Blocks.ENDER_CHEST;}
        else if (primary.getValue() == Primary.CryingObsidian) {index = Blocks.CRYING_OBSIDIAN;}
        else if (primary.getValue() == Primary.NetheriteBlock) {index = Blocks.NETHERITE_BLOCK;}
        else if (primary.getValue() == Primary.AncientDebris) {index = Blocks.ANCIENT_DEBRIS;}
        else if (primary.getValue() == Primary.RespawnAnchor) {index = Blocks.RESPAWN_ANCHOR;}
        else if (primary.getValue() == Primary.Anvil) {index = Blocks.ANVIL;}
        return index;
    }

    private int findBlock() {
        int index = InventoryUtils.findBlockInHotbar(primaryBlock());
        if (index == -1 && allBlocks.getValue()) {
            if (index == -1) index = InventoryUtils.findBlockInHotbar(Blocks.OBSIDIAN);
            if (index == -1) index = InventoryUtils.findBlockInHotbar(Blocks.CRYING_OBSIDIAN);
            if (index == -1) index = InventoryUtils.findBlockInHotbar(Blocks.NETHERITE_BLOCK);
            if (index == -1) index = InventoryUtils.findBlockInHotbar(Blocks.ANCIENT_DEBRIS);
            if (index == -1) index = InventoryUtils.findBlockInHotbar(Blocks.ENDER_CHEST);
            if (index == -1) index = InventoryUtils.findBlockInHotbar(Blocks.RESPAWN_ANCHOR);
            if (index == -1) index = InventoryUtils.findBlockInHotbar(Blocks.ANVIL);
        }
        return index;
    }

    @Override
    public void onEnable() {
        lastPos = MC.player.getBlockPos();

        if(snap.getValue() && doSnap == true) {
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
            MC.player.updatePosition(xPos, MC.player.getY(), zPos);
            MC.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.PositionOnly(MC.player.getX(), MC.player.getY(), MC.player.getZ(), MC.player.isOnGround()));
        }
    }

    @Override
    public void onDisable() {
        ticks = 0;
        doSnap = true;
        timeToStart = 0;
    }
}
