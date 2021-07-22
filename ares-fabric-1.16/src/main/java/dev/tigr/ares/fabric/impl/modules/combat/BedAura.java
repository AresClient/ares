package dev.tigr.ares.fabric.impl.modules.combat;

import com.google.common.collect.Streams;
import dev.tigr.ares.core.feature.module.Category;
import dev.tigr.ares.core.feature.module.Module;
import dev.tigr.ares.core.setting.Setting;
import dev.tigr.ares.core.setting.settings.BooleanSetting;
import dev.tigr.ares.core.setting.settings.EnumSetting;
import dev.tigr.ares.core.setting.settings.numerical.DoubleSetting;
import dev.tigr.ares.core.setting.settings.numerical.FloatSetting;
import dev.tigr.ares.core.setting.settings.numerical.IntegerSetting;
import dev.tigr.ares.core.util.Pair;
import dev.tigr.ares.core.util.Timer;
import dev.tigr.ares.core.util.global.Utils;
import dev.tigr.ares.core.util.render.Color;
import dev.tigr.ares.fabric.event.client.PacketEvent;
import dev.tigr.ares.fabric.mixin.accessors.PlayerMoveC2SPacketAccessor;
import dev.tigr.ares.fabric.utils.Comparators;
import dev.tigr.ares.fabric.utils.InventoryUtils;
import dev.tigr.ares.fabric.utils.RenderUtils;
import dev.tigr.ares.fabric.utils.WorldUtils;
import dev.tigr.simpleevents.listener.EventHandler;
import dev.tigr.simpleevents.listener.EventListener;
import net.minecraft.block.AirBlock;
import net.minecraft.block.BedBlock;
import net.minecraft.block.BlockState;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.entity.Entity;
import net.minecraft.item.BedItem;
import net.minecraft.item.EnchantedGoldenAppleItem;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import java.util.stream.Collectors;

import static dev.tigr.ares.fabric.impl.modules.combat.CrystalAura.getDamage;
import static dev.tigr.ares.fabric.impl.modules.combat.CrystalAura.rayTrace;

/**
 * @author Tigermouthbear 2/6/21
 */
@Module.Info(name = "BedAura", description = "Automatically places and explodes beds in the nether or end for combat", category = Category.COMBAT)
public class BedAura extends Module {
    // TODO: Simpler 1.15+ mode which focuses on placing on player rather than calculating. Better rotations. Antisuicide / max self damage. Smart Break?
    private final Setting<Target> targetSetting = register(new EnumSetting<>("Target", Target.CLOSEST));
    private final Setting<Mode> placeMode = register(new EnumSetting<>("Place Mode", Mode.DAMAGE));
//    private final Setting<Boolean> preventSuicide = register(new BooleanSetting("Prevent Suicide", true));
    private final Setting<Boolean> noGappleSwitch = register(new BooleanSetting("No Gapple Switch", false));
    private final Setting<Integer> placeDelay = register(new IntegerSetting("Place Delay", 12, 0, 15));
    private final Setting<Integer> breakDelay = register(new IntegerSetting("Break Delay", 1, 0, 15));
    private final Setting<Float> minDamage = register(new FloatSetting("Minimum Damage", 7.5f, 0, 15));
    private final Setting<Double> placeRange = register(new DoubleSetting("Place Range", 5, 0, 10));
    private final Setting<Double> breakRange = register(new DoubleSetting("Break Range", 5, 0, 10));
    private final Setting<Integer> replenishSlot = register(new IntegerSetting("Replenish Slot", 8, 1, 9));
    private final Setting<Boolean> silentSwitch = register(new BooleanSetting("Silent Switch", true)).setVisibility(() -> Math.max(breakDelay.getValue(), placeDelay.getValue()) > 0);
    private final Setting<Boolean> sync = register(new BooleanSetting("Sync", true));
    private final Setting<Boolean> oneDotTwelve = register(new BooleanSetting("1.12", false));

    private final Setting<Boolean> showRenderOptions = register(new BooleanSetting("Show Render Options", false));
    private final Setting<Boolean> renderAir = register(new BooleanSetting("Render While Air", false));
    private final Setting<Float> colorRed = register(new FloatSetting("Red", 1, 0, 1)).setVisibility(showRenderOptions::getValue);
    private final Setting<Float> colorGreen = register(new FloatSetting("Green", 1, 0, 1)).setVisibility(showRenderOptions::getValue);
    private final Setting<Float> colorBlue = register(new FloatSetting("Blue", 0.45f, 0, 1)).setVisibility(showRenderOptions::getValue);
    private final Setting<Float> fillAlpha = register(new FloatSetting("Fill Alpha", 0.24f, 0, 1)).setVisibility(showRenderOptions::getValue);
    private final Setting<Float> boxAlpha = register(new FloatSetting("Line Alpha", 1f, 0, 1)).setVisibility(showRenderOptions::getValue);
    private final Setting<Float> lineThickness = register(new FloatSetting("Line Weight", 2f, 0f, 10f)).setVisibility(showRenderOptions::getValue);

    enum Mode { DAMAGE, DISTANCE }
    enum Target { CLOSEST, MOST_DAMAGE }

    private final Timer logicTimer = new Timer();
    private double[] rotations = null;
    public Pair<BlockPos, Direction> target = null;
    private final Stack<BlockPos> placed = new Stack<>();

    private Box renderBox = null;
    private final Timer renderTimer = new Timer();

    @Override
    public void onTick() {
        if(!MC.world.getDimension().isBedWorking()) run();
    }

    private void run() {
        // remove bed poses from world if not a bed anymore
        placed.removeIf(pos -> !(MC.world.getBlockState(pos).getBlock() instanceof BedBlock));

        // reset rotations
        if(rotations != null) rotations = null;

        // cleanup render
        if(renderTimer.passedSec(3)) {
            target = null;
        }

        // Check player has beds
        if(amountBedInInventory() <= 0 && placed.isEmpty()) return;

        // replenish
        replenishBed();

        if(amountBedInHotbar() <= 0 && placed.isEmpty()) return;

        // do logic
        place();
        explode();
    }

    private void place() {
        if(logicTimer.passedTicks(placeDelay.getValue()) && placed.isEmpty()) {
            // if no gapple switch and player is holding apple
            if(noGappleSwitch.getValue() && MC.player.inventory.getMainHandStack().getItem() instanceof EnchantedGoldenAppleItem) {
                if(target != null) target = null;
                return;
            }

            // find best crystal spot
            Pair<BlockPos, Direction> target = getBestPlacement();
            if(target == null) return;

            placeBed(target);
            logicTimer.reset();
        }
    }

    private void placeBed(Pair<BlockPos, Direction> pair) {
        int oldSelection = -1;
        if(silentSwitch.getValue() && Math.max(breakDelay.getValue(), placeDelay.getValue()) > 0)
            oldSelection = MC.player.inventory.selectedSlot;
        // switch to crystals if not holding
        if(!(MC.player.inventory.getMainHandStack().getItem() instanceof BedItem)) {
            int slot = -1;
            for(int i = 0; i < 9; i++) {
                if(MC.player.inventory.getStack(i).getItem() instanceof BedItem) {
                    slot = i;
                    break;
                }
            }
            if(slot != -1) {
                MC.player.inventory.selectedSlot = slot;
                if(sync.getValue()) MC.player.networkHandler.sendPacket(new UpdateSelectedSlotC2SPacket());
            }
        }

        // place
        placeRotated(pair.getFirst(), pair.getSecond());
        placed.add(pair.getFirst());

        // Swap back
        if(silentSwitch.getValue() && Math.max(breakDelay.getValue(), placeDelay.getValue()) > 0 && oldSelection != -1)
            MC.player.inventory.selectedSlot = oldSelection;

        // set render pos
        target = pair;
    }

    private void placeRotated(BlockPos pos, Direction direction) {
        float yaw = direction.asRotation();
        MC.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.LookOnly(yaw, MC.player.pitch, MC.player.isOnGround()));
        WorldUtils.placeBlockNoRotate(Hand.MAIN_HAND, pos);
        rotations = new double[] { yaw, MC.player.pitch };
    }

    private void explode() {
        if(!logicTimer.passedTicks(breakDelay.getValue()) || placed.isEmpty()) return;

        BlockPos pos = placed.peek();
        Vec3d vec = new Vec3d(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5);
        MC.interactionManager.interactBlock(MC.player, MC.world, Hand.MAIN_HAND, new BlockHitResult(vec, Direction.UP, pos, true));

        // spoof rotations
        rotations = WorldUtils.calculateLookAt(vec.x, vec.y, vec.z, MC.player);

        // reset timer
        logicTimer.reset();
    }

    @EventHandler
    public EventListener<PacketEvent.Sent> packetSentEvent = new EventListener<>(event -> {
        // rotation spoofing
        if(event.getPacket() instanceof PlayerMoveC2SPacket && rotations != null) {
            ((PlayerMoveC2SPacketAccessor) event.getPacket()).setPitch((float) rotations[1]);
            ((PlayerMoveC2SPacketAccessor) event.getPacket()).setYaw((float) rotations[0]);
        }
    });

    // draw target
    @Override
    public void onRender3d() {
        Color fillColor = new Color(colorRed.getValue(), colorGreen.getValue(), colorBlue.getValue(), fillAlpha.getValue());
        Color outlineColor = new Color(colorRed.getValue(), colorGreen.getValue(), colorBlue.getValue(), boxAlpha.getValue());

        if(target != null) {
            if(MC.world.getBlockState(target.getFirst()).getBlock() instanceof BedBlock) {
                switch (target.getSecond()) {
                    case NORTH:
                        renderBox = RenderUtils.getBoundingBox(target.getFirst()).expand(0, 0, 0.5).offset(0, 0, -0.5);
                        renderTimer.reset();
                        break;
                    case WEST:
                        renderBox = RenderUtils.getBoundingBox(target.getFirst()).expand(0.5, 0, 0).offset(-0.5, 0, 0);
                        renderTimer.reset();
                        break;
                    case SOUTH:
                        renderBox = RenderUtils.getBoundingBox(target.getFirst()).expand(0, 0, 0.5).offset(0, 0, 0.5);
                        renderTimer.reset();
                        break;
                    case EAST:
                        renderBox = RenderUtils.getBoundingBox(target.getFirst()).expand(0.5, 0, 0).offset(0.5, 0, 0);
                        renderTimer.reset();
                        break;
                }
            }
        }

        if(renderBox != null) {
            if(MC.world.getBlockState(new BlockPos(renderBox.minX, renderBox.minY, renderBox.minZ)).getBlock() instanceof BedBlock) {
                renderTimer.reset();
            } else if(!renderAir.getValue()) {
                renderBox = null;
                return;
            }
            if(renderAir.getValue() && renderTimer.passedTicks(placeDelay.getValue() +5)) {
                renderBox = null;
                return;
            }
            RenderUtils.renderBlock(renderBox
                            .offset(-MC.gameRenderer.getCamera().getPos().x, -MC.gameRenderer.getCamera().getPos().y, -MC.gameRenderer.getCamera().getPos().z),
                    fillColor, outlineColor, lineThickness.getValue()
            );
        }
    }

//    private boolean canBreakBed(Pair<BlockPos, Direction> pair) {
//        return MC.player.squaredDistanceTo(pair.getFirst().getX(), pair.getFirst().getY(), pair.getFirst().getZ()) <= breakRange.getValue() * breakRange.getValue() // check range
//        && !(MC.player.getHealth() - getDamage(new Vec3d(pair.getFirst().getX() + 0.5 + pair.getSecond().getOffsetX() / 2d, pair.getFirst().getY() + 0.5, pair.getFirst().getZ() + 0.5 + pair.getSecond().getOffsetZ() / 2d), MC.player) <= 1 && preventSuicide.getValue()); // check suicide
//    }

    private Pair<BlockPos, Direction> getBestPlacement() {
        double bestScore = 69420;
        Pair<BlockPos, Direction> target = null;
        for(Entity targetedPlayer: getTargets()) {
            // find best location to place
            List<BlockPos> targetsBlocks = getPlaceableBlocks(targetedPlayer);
            List<BlockPos> blocks = getPlaceableBlocks(MC.player);

            for(BlockPos pos: blocks) {
                if(!targetsBlocks.contains(pos) || (double) getDamage(new Vec3d(pos.getX() + 0.5, pos.getY() + 1, pos.getZ() + 0.5), targetedPlayer) < minDamage.getValue())
                    continue;

                double score = getScore(pos, targetedPlayer);

                // find best place for bed part
                if(target == null || (score < bestScore && score != -1)) {
                    // find direction to place
                    Pair<BlockPos, Direction> placement = getBedPlacement(pos);
                    if(placement != null) {
                        target = placement;
                        bestScore = score;
                    }
                }
            }
        }
        return target;
    }

    private Pair<BlockPos, Direction> getBedPlacement(BlockPos pos) {
        BlockState north = MC.world.getBlockState(pos.north());
        BlockState east = MC.world.getBlockState(pos.east());
        BlockState south = MC.world.getBlockState(pos.south());
        BlockState west = MC.world.getBlockState(pos.west());

        if((!oneDotTwelve.getValue() ? north.getMaterial().isReplaceable() : north.getBlock() instanceof AirBlock)
                && MC.world.getNonSpectatingEntities(Entity.class, new Box(pos.north())).stream().noneMatch(Entity::collides)) {
            if(!oneDotTwelve.getValue() || MC.world.getBlockState(pos.down()).isFullCube(MC.world, pos.down())
                    && MC.world.getBlockState(pos.north().down()).isFullCube(MC.world, pos.north().down()))
                return new Pair<>(pos.north(), Direction.SOUTH);
        }

        if((!oneDotTwelve.getValue() ? east.getMaterial().isReplaceable() : east.getBlock() instanceof AirBlock)
                && MC.world.getNonSpectatingEntities(Entity.class, new Box(pos.east())).stream().noneMatch(Entity::collides)) {
            if(!oneDotTwelve.getValue() || MC.world.getBlockState(pos.down()).isFullCube(MC.world, pos.down())
                    && MC.world.getBlockState(pos.east().down()).isFullCube(MC.world, pos.north().down()))
                return new Pair<>(pos.east(), Direction.WEST);
        }

        if((!oneDotTwelve.getValue() ? south.getMaterial().isReplaceable() : south.getBlock() instanceof AirBlock)
                && MC.world.getNonSpectatingEntities(Entity.class, new Box(pos.south())).stream().noneMatch(Entity::collides)) {
            if(!oneDotTwelve.getValue() || MC.world.getBlockState(pos.down()).isFullCube(MC.world, pos.down())
                    && MC.world.getBlockState(pos.south().down()).isFullCube(MC.world, pos.north().down()))
                return new Pair<>(pos.south(), Direction.NORTH);
        }

        if((!oneDotTwelve.getValue() ? west.getMaterial().isReplaceable() : west.getBlock() instanceof AirBlock)
                && MC.world.getNonSpectatingEntities(Entity.class, new Box(pos.west())).stream().noneMatch(Entity::collides)) {
            if(!oneDotTwelve.getValue() || MC.world.getBlockState(pos.down()).isFullCube(MC.world, pos.down())
                    && MC.world.getBlockState(pos.west().down()).isFullCube(MC.world, pos.north().down()))
                return new Pair<>(pos.west(), Direction.EAST);
        }

        return null;
    }

    // utils
    private double getScore(BlockPos pos, Entity player) {
        double score;
        if(placeMode.getValue() == Mode.DISTANCE) {
            score = Math.abs(player.getY() - pos.up().getY())
                    + Math.abs(player.getX() - pos.getX())
                    + Math.abs(player.getZ() - pos.getZ());

            if(rayTrace(
                    new Vec3d(pos.getX() + 0.5, pos.getY() + 1, pos.getZ() + 0.5),
                    new Vec3d(player.getPos().x,
                            player.getPos().y,
                            player.getPos().z))

                    == HitResult.Type.BLOCK) score = -1;
        } else {
            score = 200 - getDamage(new Vec3d(pos.getX() + 0.5, pos.getY() + 1, pos.getZ() + 0.5), player);
        }

        return score;
    }

    private List<Entity> getTargets() {
        List<Entity> targets = new ArrayList<>();

        if(targetSetting.getValue() == Target.CLOSEST) {
            targets.addAll(Streams.stream(MC.world.getEntities()).filter(this::isValidTarget).collect(Collectors.toList()));
            targets.sort(Comparators.entityDistance);
        } else if(targetSetting.getValue() == Target.MOST_DAMAGE) {
            for(Entity entity: MC.world.getEntities()) {
                if(!isValidTarget(entity))
                    continue;
                targets.add(entity);
            }
        }

        return targets;
    }

    private boolean isValidTarget(Entity entity) {
        return WorldUtils.isValidTarget(entity, Math.max(placeRange.getValue(), breakRange.getValue()) + 8);
    }

    private List<BlockPos> getPlaceableBlocks(Entity player) {
        List<BlockPos> square = new ArrayList<>();

        int range = (int) Utils.roundDouble(placeRange.getValue(), 0);
        BlockPos pos = player.getBlockPos();
        for(int x = -range; x <= range; x++)
            for(int y = -range; y <= range; y++)
                for(int z = -range; z <= range; z++)
                    square.add(pos.add(x, y, z));

        return square.stream().filter(blockPos -> canBedBePlacedHere(blockPos) && MC.player.squaredDistanceTo(blockPos.getX() + 0.5, blockPos.getY(), blockPos.getZ() + 0.5) <= (range * range)).collect(Collectors.toList());
    }

    private boolean canBedBePlacedHere(BlockPos pos) {
        if(oneDotTwelve.getValue()) {
            return (MC.world.getBlockState(pos).getMaterial().isReplaceable() && MC.world.getBlockState(pos.down()).isFullCube(MC.world, pos.down()))
                    && (MC.world.getBlockState(pos.north()).getBlock() instanceof AirBlock && MC.world.getBlockState(pos.north().down()).isFullCube(MC.world, pos.north().down())
                    || MC.world.getBlockState(pos.east()).getBlock() instanceof AirBlock && MC.world.getBlockState(pos.east().down()).isFullCube(MC.world, pos.east().down())
                    || MC.world.getBlockState(pos.south()).getBlock() instanceof AirBlock && MC.world.getBlockState(pos.south().down()).isFullCube(MC.world, pos.south().down())
                    || MC.world.getBlockState(pos.west()).getBlock() instanceof AirBlock && MC.world.getBlockState(pos.west().down()).isFullCube(MC.world, pos.west().down())
            );
        } else {
            return MC.world.getBlockState(pos).getMaterial().isReplaceable()
                    && (MC.world.getBlockState(pos.north()).getMaterial().isReplaceable()
                    || MC.world.getBlockState(pos.east()).getMaterial().isReplaceable()
                    || MC.world.getBlockState(pos.south()).getMaterial().isReplaceable()
                    || MC.world.getBlockState(pos.west()).getMaterial().isReplaceable()
            );
        }
    }

    private int amountBedInInventory() {
        int quantity = 0;

        for(int i = 0; i <= 44; i++) {
            ItemStack stackInSlot = MC.player.inventory.getStack(i);
            if(stackInSlot.getItem() instanceof BedItem) quantity += stackInSlot.getCount();
        }

        return quantity;
    }

    private int amountBedInHotbar() {
        int quantity = 0;

        for(int i = 0; i < 9; i++) {
            ItemStack stackInSlot = MC.player.inventory.getStack(i);
            if(stackInSlot.getItem() instanceof BedItem) quantity += stackInSlot.getCount();
        }

        return quantity;
    }

    private void replenishBed() {
        int slot = replenishSlot.getValue() - 1;
        if(MC.player.inventory.getStack(slot).getItem() instanceof BedItem) return;
        if(MC.currentScreen == null || MC.currentScreen instanceof InventoryScreen) {
            for(int i = 45; i > 8; i--) {
                if(MC.player.inventory.getStack(i).getItem() instanceof BedItem) {
                    if(InventoryUtils.getHotbarBlank() != slot) {
                        if(MC.player.inventory.getStack(slot).isEmpty()) {
                            MC.interactionManager.clickSlot(MC.player.currentScreenHandler.syncId, InventoryUtils.getSlotIndex(i), 0, SlotActionType.PICKUP, MC.player);
                            MC.interactionManager.clickSlot(MC.player.currentScreenHandler.syncId, InventoryUtils.getSlotIndex(slot), 0, SlotActionType.PICKUP, MC.player);
                        } else {
                            MC.interactionManager.clickSlot(MC.player.currentScreenHandler.syncId, InventoryUtils.getSlotIndex(i), 0, SlotActionType.PICKUP, MC.player);
                            MC.interactionManager.clickSlot(MC.player.currentScreenHandler.syncId, InventoryUtils.getSlotIndex(slot), 0, SlotActionType.PICKUP, MC.player);
                            MC.interactionManager.clickSlot(MC.player.currentScreenHandler.syncId, InventoryUtils.getSlotIndex(i), 0, SlotActionType.PICKUP, MC.player); // return any items that may have been there to i
                        }

                    } else {
                        MC.interactionManager.clickSlot(MC.player.currentScreenHandler.syncId, InventoryUtils.getSlotIndex(i), 0, SlotActionType.QUICK_MOVE, MC.player);
                    }
                    return;
                }
            }
        }
    }
}
