package dev.tigr.ares.forge.impl.modules.combat;

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
import dev.tigr.ares.forge.impl.modules.player.Freecam;
import dev.tigr.ares.forge.utils.InventoryUtils;
import dev.tigr.ares.forge.utils.WorldUtils;
import dev.tigr.ares.forge.utils.render.RenderUtils;
import net.minecraft.block.BlockAir;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;

import java.util.*;

import static dev.tigr.ares.forge.impl.modules.player.RotationManager.ROTATIONS;

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
    private final Setting<Boolean> air = register(new BooleanSetting("Air-place", false));
    private final Setting<Boss> boss = register(new EnumSetting<>("Boss", Boss.NONE));
    private final Setting<Boolean> renderFinished = register(new BooleanSetting("Render Finished", true));
    private final Setting<Integer> renderAlpha = register(new IntegerSetting("Render Alpha", 40, 0, 100));

    enum Boss {NONE, BOSSPLUS, BOSS}

    private LinkedHashMap<BlockPos, Pair<Timer, Boolean>> renderChange = new LinkedHashMap<>();

    int key = Priorities.Rotation.SURROUND;

    private BlockPos lastPos = new BlockPos(0, -100, 0);
    private int ticks = 0;

    private boolean hasCentered = false;
    private Timer onGroundCenter = new Timer();

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
        if(onGroundCenter.passedTicks(centerDelay.getValue()) && snap.getValue() && doSnap && !hasCentered && MC.player.onGround) {
            WorldUtils.snapPlayer(lastPos);
            hasCentered = true;
        }

        if(!hasCentered && !MC.player.onGround) {
            onGroundCenter.reset();
        }

        BlockPos roundedPos = WorldUtils.roundBlockPos(MC.player.getPositionVector());
        if(onlyGround.getValue() && !MC.player.onGround && roundedPos.getY() <= lastPos.getY()) {
            lastPos = WorldUtils.roundBlockPos(MC.player.getPositionVector());
        }

        // Check if a location is still needing placing and release rotations if nowhere is
        boolean flagCurrent = true;
        for(BlockPos pos: getPositions()) {
            if(placeOnCrystal.getValue())
                if(!bossList().contains(pos))
                    if(MC.world.getBlockState(pos).getBlock() instanceof BlockAir)
                        flagCurrent = false;

            if(MC.world.getBlockState(pos) instanceof BlockAir && !MC.world.getEntitiesWithinAABBExcludingEntity(null, new AxisAlignedBB(pos)).stream().noneMatch(Entity::canBeCollidedWith))
                flagCurrent = false;
        }
        if(ROTATIONS.isKeyCurrent(key) && flagCurrent) ROTATIONS.setCompletedAction(key, true);

        if(MC.player.onGround || !onlyGround.getValue()) {
            if(delay.getValue() != 0 && ticks++ % delay.getValue() != 0) return;

            // make sure player is in the same place
            AbstractClientPlayer loc = Freecam.INSTANCE.getEnabled() ? Freecam.INSTANCE.clone : MC.player;
            BlockPos locRounded = WorldUtils.roundBlockPos(loc.getPositionVector());
            if(!lastPos.equals(loc.onGround ? locRounded : loc.getPosition())) {
                if(onlyGround.getValue() || !(loc.getPositionVector().y <= lastPos.getY() + 1.5)
                        || ((Math.floor(loc.getPositionVector().x) != lastPos.getX() || Math.floor(loc.getPositionVector().z) != lastPos.getZ()) && !(loc.getPositionVector().y <= lastPos.getY() + 0.75))
                        || (!MC.world.getBlockState(lastPos).getMaterial().isReplaceable() && loc.getPosition() != lastPos)
                ) {
                    setEnabled(false);
                    return;
                }
                if(!onlyGround.getValue() && locRounded.getY() <= lastPos.getY()) {
                    lastPos = locRounded;
                }
            }

            // find obby
            int obbyIndex = InventoryUtils.findBlockInHotbar(Blocks.OBSIDIAN);
            if(obbyIndex == -1) return;
            int prevSlot = MC.player.inventory.currentItem;

            if(needsToPlace()) {
                for(BlockPos pos : getPositions()) {
                    if(MC.world.getBlockState(pos).getMaterial().isReplaceable())
                        renderChange.putIfAbsent(pos, new Pair<>(new Timer(), false));

                    MC.player.inventory.currentItem = obbyIndex;

                    boolean bossPos = false;
                    if(bossList().contains(pos)) bossPos = true;

                    if(WorldUtils.placeBlockMainHand(rotate.getValue(), key, key, delay.getValue() <= 0, false, pos, air.getValue(), !bossPos && placeOnCrystal.getValue())) {
                        if(renderChange.containsKey(pos)) {
                            renderChange.get(pos).setSecond(true);
                            renderChange.get(pos).getFirst().reset();
                        }
                        if(delay.getValue() != 0) {
                            MC.player.inventory.currentItem = prevSlot;
                            return;
                        }
                    }
                }

                MC.player.inventory.currentItem = prevSlot;
            }
        }
    }

    private boolean needsToPlace() {
        return anyAir(lastPos.north(), lastPos.east(), lastPos.south(), lastPos.west(),
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
        lastPos = MC.player.onGround ? WorldUtils.roundBlockPos(MC.player.getPositionVector()) : MC.player.getPosition();
    }

    @Override
    public void onDisable() {
        ticks = 0;
        doSnap = true;
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
            AxisAlignedBB render = new AxisAlignedBB(pos);

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
