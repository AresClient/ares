package dev.tigr.ares.fabric.impl.modules.combat;

import dev.tigr.ares.core.feature.module.Category;
import dev.tigr.ares.core.feature.module.Module;
import dev.tigr.ares.core.setting.Setting;
import dev.tigr.ares.core.setting.settings.BooleanSetting;
import dev.tigr.ares.core.setting.settings.EnumSetting;
import dev.tigr.ares.core.setting.settings.numerical.IntegerSetting;
import dev.tigr.ares.core.util.Pair;
import dev.tigr.ares.core.util.Priorities;
import dev.tigr.ares.core.util.Timer;
import dev.tigr.ares.core.util.render.Color;
import dev.tigr.ares.fabric.impl.modules.player.Freecam;
import dev.tigr.ares.fabric.utils.InventoryUtils;
import dev.tigr.ares.fabric.utils.WorldUtils;
import dev.tigr.ares.fabric.utils.entity.SelfUtils;
import dev.tigr.ares.fabric.utils.render.RenderUtils;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.ShapeContext;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;

import java.util.*;

/**
 * @author Tigermouthbear
 */
@Module.Info(name = "Surround", description = "Surrounds your feet with obsidian", category = Category.COMBAT)
public class Surround extends Module {
    public static Surround INSTANCE;

    private final Setting<Integer> delay = register(new IntegerSetting("Delay", 0, 0, 10));
    private final Setting<Boolean> onlyGround = register(new BooleanSetting("Only On Ground", false));
    private final Setting<Boolean> snap = register(new BooleanSetting("Center", true));
    private final Setting<Integer> centerDelay = register(new IntegerSetting("Center Delay", 0, 0, 10));
    private final Setting<Boolean> placeOnCrystal = register(new BooleanSetting("Place on Crystal", true));
    private final Setting<Boolean> rotate = register(new BooleanSetting("Rotate", true));
    private final Setting<Boolean> packetPlace = register(new BooleanSetting("Packet Place", true));
    private final Setting<Boolean> air = register(new BooleanSetting("AirPlace", true));
    private final Setting<Boolean> airplaceForce = register(new BooleanSetting("Force AirPlace", false)).setVisibility(air::getValue);
    private final Setting<Boss> boss = register(new EnumSetting<>("Boss", Boss.NONE));
    private final Setting<Primary> primary = register(new EnumSetting<>("Main", Primary.Obsidian));
    private final Setting<Boolean> allBlocks = register(new BooleanSetting("All BP Blocks", true));
    private final Setting<Boolean> renderFinished = register(new BooleanSetting("Render Finished", true));
    private final Setting<Integer> renderAlpha = register(new IntegerSetting("Render Alpha", 40, 0, 100));

    enum Primary {Obsidian, EnderChest, CryingObsidian, NetheriteBlock, AncientDebris, RespawnAnchor, Anvil}
    enum Boss {NONE, BOSSPLUS, BOSS}

    int key = Priorities.Rotation.SURROUND;

    private LinkedHashMap<BlockPos, Pair<Timer, Boolean>> renderChange = new LinkedHashMap<>();

    private BlockPos lastPos = new BlockPos(0, -100, 0);
    private int ticks = 0;

    private boolean hasCentered = false;
    private Timer onGroundCenter = new Timer();

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
        if(onGroundCenter.passedTicks(centerDelay.getValue()) && snap.getValue() && doSnap && !hasCentered && MC.player.isOnGround()) {
            SelfUtils.snapPlayer(lastPos);
            hasCentered = true;
        }

        if(!hasCentered && !MC.player.isOnGround()) {
            onGroundCenter.reset();
        }

        BlockPos roundedPos = WorldUtils.roundBlockPos(MC.player.getPos());
        if(onlyGround.getValue() && !MC.player.isOnGround() && roundedPos.getY() <= lastPos.getY()) {
            lastPos = WorldUtils.roundBlockPos(MC.player.getPos());
        }

        // Check if a location is still needing placing and release rotations if nowhere is
        boolean flagCurrent = true;
        for(BlockPos pos: getPositions()) {
            if(placeOnCrystal.getValue())
                if(!bossList().contains(pos))
                    if(MC.world.getBlockState(pos).isAir())
                        flagCurrent = false;

            if(MC.world.getBlockState(pos).isAir() && MC.world.canPlace(Blocks.OBSIDIAN.getDefaultState(), pos, ShapeContext.absent()))
                flagCurrent = false;
        }
        if(ROTATIONS.isKeyCurrent(key) && flagCurrent) ROTATIONS.setCompletedAction(key, true);

        if(surroundInstanceDelay.passedMillis(timeToStart) && (MC.player.isOnGround() || !onlyGround.getValue())) {
            if(delay.getValue() != 0 && ticks++ % delay.getValue() != 0) return;

            // make sure player is in the same place
            AbstractClientPlayerEntity loc = Freecam.INSTANCE.getEnabled() ? Freecam.INSTANCE.clone : MC.player;
            BlockPos locRounded = WorldUtils.roundBlockPos(loc.getPos());
            if(!lastPos.equals(loc.isOnGround() ? locRounded : loc.getBlockPos())) {
                if(onlyGround.getValue() || !(loc.getPos().y <= lastPos.getY() + 1.5)
                        || ((Math.floor(loc.getPos().x) != lastPos.getX() || Math.floor(loc.getPos().z) != lastPos.getZ()) && !(loc.getPos().y <= lastPos.getY() + 0.75))
                        || (!MC.world.getBlockState(lastPos).getMaterial().isReplaceable() && !loc.getBlockPos().equals(lastPos))
                ) {
                    setEnabled(false);
                    return;
                }
                if(!onlyGround.getValue() && locRounded.getY() <= lastPos.getY()) {
                    lastPos = locRounded;
                }
            }

            // find obby
            int obbyIndex = findBlock();
            if(obbyIndex == -1) return;
            int prevSlot = MC.player.inventory.selectedSlot;

            if(needsToPlace()) {
                for(BlockPos pos : getPositions()) {
                    if(MC.world.getBlockState(pos).getMaterial().isReplaceable())
                        renderChange.putIfAbsent(pos, new Pair<>(new Timer(), false));

                    if(delay.getValue() == 0) {
                        HOTBAR_TRACKER.setSlot(obbyIndex, packetPlace.getValue(), prevSlot);
                    }

                    boolean bossPos = false;
                    if(bossList().contains(pos)) bossPos = true;

                    if(SelfUtils.placeBlockMainHand(packetPlace.getValue(), delay.getValue() == 0 ? -1 : obbyIndex, rotate.getValue(), key, key, delay.getValue() <= 0, false, pos, air.getValue(), airplaceForce.getValue(), !bossPos && placeOnCrystal.getValue())) {
                        if(renderChange.containsKey(pos)) {
                            renderChange.get(pos).setSecond(true);
                            renderChange.get(pos).getFirst().reset();
                        }
                        if(delay.getValue() != 0) return;
                    }
                }

                if(delay.getValue() == 0) {
                    HOTBAR_TRACKER.reset();
                }
            }
        }
    }

    private boolean needsToPlace() {
        return anyAir(lastPos.down(), lastPos.north(), lastPos.east(), lastPos.south(), lastPos.west(),
                lastPos.north(2), lastPos.east(2), lastPos.south(2), lastPos.west(2),
                lastPos.north().east(), lastPos.east().south(), lastPos.south().west(), lastPos.west().north()
        );
    }

    // returns list of places blocks should be placed at
    private List<BlockPos> getPositions() {
        List<BlockPos> positions = new ArrayList<>();

        if(!onlyGround.getValue()) add(positions, lastPos.down());
        add(positions, lastPos.north());
        add(positions, lastPos.east());
        add(positions, lastPos.south());
        add(positions, lastPos.west());

        if(boss.getValue() != Boss.NONE)
            for(BlockPos pos: bossList())
                add(positions, pos);

        return positions;
    }

    private List<BlockPos> bossList() {
        List<BlockPos> positions = new ArrayList<>();

        if(MC.world.getBlockState(lastPos.north()).getBlock() != Blocks.BEDROCK) add(positions, lastPos.north(2));
        if(MC.world.getBlockState(lastPos.east()).getBlock() != Blocks.BEDROCK) add(positions, lastPos.east(2));
        if(MC.world.getBlockState(lastPos.south()).getBlock() != Blocks.BEDROCK) add(positions, lastPos.south(2));
        if(MC.world.getBlockState(lastPos.west()).getBlock() != Blocks.BEDROCK) add(positions, lastPos.west(2));

        if(boss.getValue() == Boss.BOSSPLUS) {
            if(MC.world.getBlockState(lastPos.north()).getBlock() != Blocks.BEDROCK || MC.world.getBlockState(lastPos.east()).getBlock() != Blocks.BEDROCK)
                add(positions, lastPos.north().east());
            if(MC.world.getBlockState(lastPos.east()).getBlock() != Blocks.BEDROCK || MC.world.getBlockState(lastPos.south()).getBlock() != Blocks.BEDROCK)
                add(positions, lastPos.east().south());
            if(MC.world.getBlockState(lastPos.south()).getBlock() != Blocks.BEDROCK || MC.world.getBlockState(lastPos.west()).getBlock() != Blocks.BEDROCK)
                add(positions, lastPos.south().west());
            if(MC.world.getBlockState(lastPos.west()).getBlock() != Blocks.BEDROCK || MC.world.getBlockState(lastPos.north()).getBlock() != Blocks.BEDROCK)
                add(positions, lastPos.west().north());
        }

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
        lastPos = MC.player.isOnGround() ? WorldUtils.roundBlockPos(MC.player.getPos()) : MC.player.getBlockPos();
    }

    @Override
    public void onDisable() {
        ticks = 0;
        doSnap = true;
        timeToStart = 0;
        hasCentered = false;
        renderChange.clear();
        ROTATIONS.setCompletedAction(key, true);
    }

    // draw blocks
    @Override
    public void onRender3d() {
        Color airColor = new Color(1,0,0,renderAlpha.getValue().floatValue() /100);
        Color changeColor = new Color(1,1,0,renderAlpha.getValue().floatValue() /100);
        Color blockColor = new Color(0,0,1,renderAlpha.getValue().floatValue() /100);
        Color bedrockColor = new Color(0,1,0,renderAlpha.getValue().floatValue() /100);

        for(Map.Entry<BlockPos, Pair<Timer, Boolean>> entry: renderChange.entrySet()) {
            if(entry.getValue().getSecond() && entry.getValue().getFirst().passedTicks(6) && !MC.world.getBlockState(entry.getKey()).getMaterial().isReplaceable()) {
                entry.getValue().setSecond(false);
            }
        }

        RenderUtils.prepare3d();

        for (BlockPos pos : getPositions()) {
            Box render = new Box(pos);

            if(renderChange.containsKey(pos) && renderChange.get(pos).getSecond())
                RenderUtils.cubeFill(render, changeColor);

            else if(MC.world.getBlockState(pos).getMaterial().isReplaceable())
                RenderUtils.cubeFill(render, airColor);

            else if(MC.world.getBlockState(pos).getBlock() != Blocks.BEDROCK && renderFinished.getValue())
                RenderUtils.cubeFill(render, blockColor);

            else if(renderFinished.getValue())
                RenderUtils.cubeFill(render, bedrockColor);
        }

        RenderUtils.end3d();
    }
}
