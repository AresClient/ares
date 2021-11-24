package dev.tigr.ares.fabric.impl.modules.player;

import dev.tigr.ares.core.event.client.PostInitializationEvent;
import dev.tigr.ares.core.feature.module.Category;
import dev.tigr.ares.core.feature.module.Module;
import dev.tigr.ares.core.setting.Setting;
import dev.tigr.ares.core.setting.settings.BooleanSetting;
import dev.tigr.ares.core.setting.settings.EnumSetting;
import dev.tigr.ares.core.setting.settings.numerical.FloatSetting;
import dev.tigr.ares.core.util.Pair;
import dev.tigr.ares.core.util.Priorities;
import dev.tigr.ares.core.util.render.Color;
import dev.tigr.ares.fabric.event.client.PacketEvent;
import dev.tigr.ares.fabric.event.player.DamageBlockEvent;
import dev.tigr.ares.fabric.mixin.accessors.ClientPlayerInteractionManagerAccessor;
import dev.tigr.ares.fabric.utils.InventoryUtils;
import dev.tigr.ares.fabric.utils.MathUtils;
import dev.tigr.ares.fabric.utils.WorldUtils;
import dev.tigr.ares.fabric.utils.entity.SelfUtils;
import dev.tigr.ares.fabric.utils.render.RenderUtils;
import dev.tigr.simpleevents.listener.EventHandler;
import dev.tigr.simpleevents.listener.EventListener;
import net.minecraft.block.AirBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.FluidBlock;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.network.packet.c2s.play.HandSwingC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
import net.minecraft.network.packet.s2c.play.BlockUpdateS2CPacket;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.Hand;
import net.minecraft.util.math.*;

import java.util.Iterator;
import java.util.LinkedHashSet;

import static dev.tigr.ares.fabric.impl.modules.player.RotationManager.ROTATIONS;
import static dev.tigr.ares.fabric.utils.HotbarTracker.HOTBAR_TRACKER;

/***
 * @author Makrennel 08/09/21
 * A module for managing packet mining
 */
@Module.Info(name = "PacketMine", description = "Mines using packets", category = Category.PLAYER, alwaysListening = true)
public class PacketMine extends Module {
    public static PacketMine MINER;

    private final Setting<Mode> mode = register(new EnumSetting<>("Mode", Mode.UPDATE));
    private final Setting<Boolean> clientUpdate = register(new BooleanSetting("Update Client", false)).setVisibility(() -> mode.getValue() == Mode.UPDATE);
    private final Setting<Boolean> spam = register(new BooleanSetting("Spam", true)).setVisibility(() -> mode.getValue() == Mode.NORMAL);
    public final Setting<Boolean> queue = register(new BooleanSetting("Queue", true));
    private final Setting<Boolean> rotate = register(new BooleanSetting("Rotate", true));
    private final Setting<Switch> autoSwitch = register(new EnumSetting<>("AutoSwitch", Switch.SILENT));
    private final Setting<SilentOn> silentOn = register(new EnumSetting<>("Silent On", SilentOn.BLOCKUPDATE)).setVisibility(() -> autoSwitch.getValue() == Switch.SILENT);
    private final Setting<Swing> swing = register(new EnumSetting<>("Swing Type", Swing.PACKET));
    private final Setting<Boolean> spamSwing = register(new BooleanSetting("Spam Swing", false)).setVisibility(() -> mode.getValue() == Mode.UPDATE && swing.getValue() != Swing.NONE);

    private final Setting<Boolean> color = register(new BooleanSetting("Color", false));
    private final Setting<Float> red = register(new FloatSetting("Red", 0, 0, 1)).setVisibility(color::getValue);
    private final Setting<Float> green = register(new FloatSetting("Green", 1, 0, 1)).setVisibility(color::getValue);
    private final Setting<Float> blue = register(new FloatSetting("Blue", 0, 0, 1)).setVisibility(color::getValue);
    private final Setting<Float> fill = register(new FloatSetting("Fill", 0.24f, 0, 1)).setVisibility(color::getValue);
    private final Setting<Float> line = register(new FloatSetting("Line", 1, 0, 1)).setVisibility(color::getValue);

    private final Setting<Boolean> qColor = register(new BooleanSetting("Queue Color", false));
    private final Setting<Float> qRed = register(new FloatSetting("qRed", 1, 0, 1)).setVisibility(qColor::getValue);
    private final Setting<Float> qGreen = register(new FloatSetting("qGreen", 0, 0, 1)).setVisibility(qColor::getValue);
    private final Setting<Float> qBlue = register(new FloatSetting("qBlue", 0, 0, 1)).setVisibility(qColor::getValue);
    private final Setting<Float> qFill = register(new FloatSetting("qFill", 0.24f, 0, 1)).setVisibility(qColor::getValue);
    private final Setting<Float> qLine = register(new FloatSetting("qLine", 1, 0, 1)).setVisibility(qColor::getValue);

    //Reset Color Menu Visibility on initialization
    @EventHandler
    private final EventListener<PostInitializationEvent> onClientInitialized = new EventListener<>(event -> {
        color.setValue(false);
        qColor.setValue(false);
    });

    enum Mode { NORMAL, UPDATE }
    enum Swing { FULL, PACKET, NONE }
    enum Switch { NORMAL, SILENT, NONE }
    enum SilentOn { TICK, BLOCKUPDATE }

    public PacketMine() {
        MINER = this;
    }

    final int key = Priorities.Rotation.PACKET_MINE;

    private LinkedHashSet<BlockPos> posQueue = new LinkedHashSet<>();
    private BlockPos currentPos;
    private boolean hasMined;

    private float breakProgress;

    private int oldSelection = -1;
    private int toolSlot = -1;
    private boolean hasSwapped;
    boolean nextTick = false;
    boolean hasFinished = false;

    public void setTarget(BlockPos pos) {
        LinkedHashSet<BlockPos> newQueue = new LinkedHashSet<>();
        newQueue.add(pos);
        if(currentPos != null) newQueue.add(currentPos);
        newQueue.addAll(posQueue);
        currentPos = null;
        posQueue = newQueue;
        hasMined = false;
        breakProgress = 0;
        hasSwapped = false;
    }

    public void addPos(BlockPos pos) {
        posQueue.add(pos);
    }

    @Override
    public void onDisable() {
        posQueue.clear();
        currentPos = null;
        hasMined = false;
    }

    @Override
    public void onTick() {
        //Release Rotation
        releaseRotation();

        if(nullCheck()) return;

        // Check if currentPos is null and get the next queue element if it is
        pollQueueIfNull();

        // Check if currentPos is still a breakable block and remove from the queue if it no longer is
        if(checkAndClearCurrent()) {
            if(autoSwitch.getValue() == Switch.NORMAL && posQueue.isEmpty())
                doSwitchBack();
            return;
        }

        // Perform switch
        doSwitch();

        // Remove all targets out of distance
        if(removeOutOfDistance()) return;

        // Rotate
        rotate();

        // Perform mine
        mine();

        // Silent swap tick mode
        if(silentOn.getValue() == SilentOn.TICK && autoSwitch.getValue() == Switch.SILENT && (nextTick || mode.getValue() == Mode.UPDATE)) doSwitchBack();

        //Increment breakProgress if necessary
        int tool = InventoryUtils.getTool(currentPos);
        if(tool != -1) breakProgress = Math.min(breakProgress + SelfUtils.calcBlockBreakingDelta(MC.world.getBlockState(currentPos), tool), 1);
    }

    private boolean nullCheck() {
        if(MC.player == null || MC.world == null || MC.interactionManager == null) return true;
        return posQueue.isEmpty() && currentPos == null;
    }

    private void doSwitch() {
        if(hasSwapped && oldSelection != -1) return;

        oldSelection = MC.player.getInventory().selectedSlot;
        toolSlot = InventoryUtils.getTool(currentPos);

        if(autoSwitch.getValue() == Switch.SILENT && progressReady()) {
            if(oldSelection != toolSlot && toolSlot != -1) {
                HOTBAR_TRACKER.connect();
                HOTBAR_TRACKER.setSlot(toolSlot, true, -1);
                HOTBAR_TRACKER.sendSlot();
                hasSwapped = true;
            }
        }

        else if(autoSwitch.getValue() == Switch.NORMAL) {
            if(oldSelection != toolSlot && toolSlot != -1) {
                HOTBAR_TRACKER.connect();
                HOTBAR_TRACKER.setSlot(toolSlot, false, oldSelection);
                hasSwapped = true;
            }
        }

        if(mode.getValue() == Mode.NORMAL) nextTick = true;
    }

    private void doSwitchBack() {
        if(hasSwapped && oldSelection != -1) {
            HOTBAR_TRACKER.reset();
            HOTBAR_TRACKER.disconnect();
            hasSwapped = false;
            oldSelection = -1;
            toolSlot = -1;
        }

        nextTick = false;
    }

    private void releaseRotation() {
        if(currentPos == null && ROTATIONS.isKeyCurrent(key)) ROTATIONS.setCompletedAction(key, true);
    }

    private void rotate() {
        if(rotate.getValue() && currentPos != null) {
            ROTATIONS.setCurrentRotation(
                    SelfUtils.calculateLookAtVector(MathUtils.getClosestPointOfBlockPos(SelfUtils.getEyePos(), currentPos)),
                    key, key, false, false
            );
        }
    }

    private boolean removeOutOfDistance() {
        float reachSq = MC.interactionManager.getReachDistance() *MC.interactionManager.getReachDistance();

        posQueue.removeIf(p -> {
            Vec3d closest = MathUtils.getClosestPointOfBlockPos(SelfUtils.getEyePos(), p);
            return MathUtils.squaredDistanceBetween(SelfUtils.getEyePos(), closest) > reachSq;
        });

        Vec3d closest = MathUtils.getClosestPointOfBlockPos(SelfUtils.getEyePos(), currentPos);

        if(MathUtils.squaredDistanceBetween(SelfUtils.getEyePos(), closest) > reachSq) {
            currentPos = null;
            hasMined = false;
            return true;
        }

        return false;
    }

    private void pollQueueIfNull() {
        if(currentPos == null) {
            Iterator<BlockPos> i = posQueue.iterator();
            currentPos = i.next();
            i.remove();
            breakProgress = 0;
        }
    }

    private boolean checkAndClearCurrent() {
        if(MC.world.getBlockState(currentPos).getBlock() instanceof AirBlock
                || MC.world.getBlockState(currentPos).getBlock() instanceof FluidBlock
                || !WorldUtils.canBreakBlock(currentPos)
        ) {
            currentPos = null;
            hasMined = false;
            return true;
        } else return false;
    }

    private void mine() {
        Pair<Direction, Vec3d> closestVisibleSide = WorldUtils.getClosestVisibleSide(SelfUtils.getEyePos(), currentPos);
        Direction side = null;
        if(closestVisibleSide != null) side = closestVisibleSide.getFirst();
        if(side == null) {
            if(SelfUtils.getEyeY() > currentPos.getY()) side = Direction.UP;
            else side = Direction.DOWN;
        }

        if((!hasMined && mode.getValue() == Mode.NORMAL) || (spam.getValue() && mode.getValue() == Mode.NORMAL)) {
            MC.player.networkHandler.sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.START_DESTROY_BLOCK, currentPos, side));
            MC.player.networkHandler.sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.STOP_DESTROY_BLOCK, currentPos, side));
        }

        if(mode.getValue() == Mode.UPDATE) {
            if(clientUpdate.getValue()) {
                if(autoSwitch.getValue() == Switch.SILENT) updateBlockBreakingProgress(currentPos, side);
                else MC.interactionManager.updateBlockBreakingProgress(currentPos, side);
            } else {
                if(!hasMined) {
                    MC.player.networkHandler.sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.START_DESTROY_BLOCK, currentPos, side));
                    hasFinished = false;
                }
                else if(progressReady() && !hasFinished) {
                    MC.player.networkHandler.sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.STOP_DESTROY_BLOCK, currentPos, side));
                    hasFinished = true;
                }
            }
        }

        if(!hasMined || (spamSwing.getValue() && mode.getValue() == Mode.UPDATE)) {
            if(swing.getValue() == Swing.FULL) MC.player.swingHand(Hand.MAIN_HAND);
            if(swing.getValue() == Swing.PACKET) MC.player.networkHandler.sendPacket(new HandSwingC2SPacket(Hand.MAIN_HAND));
        }

        hasMined = true;
    }

    private boolean progressReady() {
        return breakProgress >= 1;
    }

    @EventHandler
    private final EventListener<DamageBlockEvent> onDamageBlock = new EventListener<>(event -> {
        if(!getEnabled() || !WorldUtils.canBreakBlock(event.getBlockPos())) return;

        if(currentPos != null) {
            if(currentPos.equals(event.getBlockPos()) && hasMined && mode.getValue() == Mode.NORMAL) {
                hasMined = false;
                event.setCancelled(true);
                return;
            } else if(currentPos.equals(event.getBlockPos())) return;
        }

        addPos(event.getBlockPos());
        if(!queue.getValue()) {
            currentPos = null;
            hasMined = false;
        }
        event.setCancelled(true);
    });

    @EventHandler
    private final EventListener<PacketEvent.Receive> onBlockUpdate = new EventListener<>(event -> {
        if(event.getPacket() instanceof BlockUpdateS2CPacket) {
            BlockUpdateS2CPacket p = (BlockUpdateS2CPacket) event.getPacket();
            if(currentPos == null) return;
            if(p.getState().getBlock() != Blocks.AIR) return;

            if(currentPos.equals(p.getPos())) {
                currentPos = null;
                hasMined = false;

                if(autoSwitch.getValue() == Switch.NORMAL && posQueue.isEmpty())
                    doSwitchBack();

                if(autoSwitch.getValue() == Switch.SILENT && silentOn.getValue() == SilentOn.BLOCKUPDATE)
                    doSwitchBack();

                //Release Rotation
                releaseRotation();
            }
            for(BlockPos blockPos: posQueue) {
                if(blockPos.equals(p.getPos())) {
                    posQueue.remove(blockPos);
                    return;
                }
            }
        }
    });

    @Override
    public void onRender3d() {
        Color cFill = new Color(red.getValue(), green.getValue(), blue.getValue(), fill.getValue());
        Color cLine = new Color(red.getValue(), green.getValue(), blue.getValue(), line.getValue());
        Color fill = new Color(qRed.getValue(), qGreen.getValue(), qBlue.getValue(), qFill.getValue());
        Color line = new Color(qRed.getValue(), qGreen.getValue(), qBlue.getValue(), qLine.getValue());

        RenderUtils.prepare3d();

        if(!posQueue.isEmpty()) {
            for(BlockPos p : posQueue) {
                Box bb = RenderUtils.getBoundingBox(p);
                if(bb != null) RenderUtils.cube(bb, fill, line);
            }
        }

        if(currentPos != null) {
            Box bb = RenderUtils.getBoundingBox(currentPos);
            if(bb != null) RenderUtils.cube(bb.contract((1 - breakProgress) /2), cFill, cLine);
        }

        RenderUtils.end3d();
    }

    private boolean updateBlockBreakingProgress(BlockPos pos, Direction direction) {
        if(!hasSwapped || toolSlot == -1) ((ClientPlayerInteractionManagerAccessor) MC.interactionManager).syncSelectedSlot();
        else MC.player.networkHandler.sendPacket(new UpdateSelectedSlotC2SPacket(toolSlot));
        if(((ClientPlayerInteractionManagerAccessor) MC.interactionManager).getBlockBreakingCooldown() > 0) {
            ((ClientPlayerInteractionManagerAccessor) MC.interactionManager).setBlockBreakingCooldown(((ClientPlayerInteractionManagerAccessor) MC.interactionManager).getBlockBreakingCooldown() -1);
            return true;
        } else {
            BlockState blockState2;
            if(MC.interactionManager.getCurrentGameMode().isCreative() && MC.world.getWorldBorder().contains(pos)) {
                ((ClientPlayerInteractionManagerAccessor) MC.interactionManager).setBlockBreakingCooldown(5);
                blockState2 = MC.world.getBlockState(pos);
                MC.getTutorialManager().onBlockBreaking(MC.world, pos, blockState2, 1.0F);
                ((ClientPlayerInteractionManagerAccessor) MC.interactionManager).sendPlayerAction(PlayerActionC2SPacket.Action.START_DESTROY_BLOCK, pos, direction);
                MC.interactionManager.breakBlock(pos);
                return true;
            } else if(((ClientPlayerInteractionManagerAccessor) MC.interactionManager).getIsCurrentlyBreaking(pos)) {
                blockState2 = MC.world.getBlockState(pos);
                if(blockState2.isAir()) {
                    ((ClientPlayerInteractionManagerAccessor) MC.interactionManager).setBreakingBlock(false);
                    return false;
                } else {
                    ((ClientPlayerInteractionManagerAccessor) MC.interactionManager).setCurrentBreakingProgress(breakProgress);
                    if(((ClientPlayerInteractionManagerAccessor) MC.interactionManager).getBlockBreakingSoundCooldown() % 4.0F == 0.0F) {
                        BlockSoundGroup blockSoundGroup = blockState2.getSoundGroup();
                        MC.getSoundManager().play(new PositionedSoundInstance(blockSoundGroup.getHitSound(), SoundCategory.BLOCKS, (blockSoundGroup.getVolume() + 1.0F) / 8.0F, blockSoundGroup.getPitch() * 0.5F, pos));
                    }

                    ((ClientPlayerInteractionManagerAccessor) MC.interactionManager).setBlockBreakingSoundCooldown(((ClientPlayerInteractionManagerAccessor) MC.interactionManager).getBlockBreakingSoundCooldown() +1);
                    MC.getTutorialManager().onBlockBreaking(MC.world, pos, blockState2, MathHelper.clamp(((ClientPlayerInteractionManagerAccessor) MC.interactionManager).getCurrentBreakingProgress(), 0.0F, 1.0F));
                    if(((ClientPlayerInteractionManagerAccessor) MC.interactionManager).getCurrentBreakingProgress() >= 1.0F) {
                        ((ClientPlayerInteractionManagerAccessor) MC.interactionManager).setBreakingBlock(false);
                        ((ClientPlayerInteractionManagerAccessor) MC.interactionManager).sendPlayerAction(PlayerActionC2SPacket.Action.STOP_DESTROY_BLOCK, pos, direction);
                        MC.interactionManager.breakBlock(pos);
                        ((ClientPlayerInteractionManagerAccessor) MC.interactionManager).setCurrentBreakingProgress(0);
                        ((ClientPlayerInteractionManagerAccessor) MC.interactionManager).setBlockBreakingSoundCooldown(0);
                        ((ClientPlayerInteractionManagerAccessor) MC.interactionManager).setBlockBreakingCooldown(5);
                    }

                    MC.world.setBlockBreakingInfo(MC.player.getId(), ((ClientPlayerInteractionManagerAccessor) MC.interactionManager).getCurrentBreakingPos(), (int)(((ClientPlayerInteractionManagerAccessor) MC.interactionManager).getCurrentBreakingProgress() * 10.0F) - 1);
                    return true;
                }
            } else return MC.interactionManager.attackBlock(pos, direction);
        }
    }
}
