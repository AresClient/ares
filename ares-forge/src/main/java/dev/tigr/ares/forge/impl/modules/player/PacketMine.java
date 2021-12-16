package dev.tigr.ares.forge.impl.modules.player;

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
import dev.tigr.ares.forge.event.events.player.DamageBlockEvent;
import dev.tigr.ares.forge.event.events.player.PacketEvent;
import dev.tigr.ares.forge.mixin.accessor.PlayerControllerMPAccessor;
import dev.tigr.ares.forge.utils.InventoryUtils;
import dev.tigr.ares.forge.utils.MathUtils;
import dev.tigr.ares.forge.utils.WorldUtils;
import dev.tigr.ares.forge.utils.entity.SelfUtils;
import dev.tigr.ares.forge.utils.render.RenderUtils;
import dev.tigr.simpleevents.listener.EventHandler;
import dev.tigr.simpleevents.listener.EventListener;
import net.minecraft.block.Block;
import net.minecraft.block.BlockAir;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.multiplayer.PlayerControllerMP;
import net.minecraft.init.Blocks;
import net.minecraft.network.play.client.CPacketAnimation;
import net.minecraft.network.play.client.CPacketHeldItemChange;
import net.minecraft.network.play.client.CPacketPlayerDigging;
import net.minecraft.network.play.server.SPacketBlockChange;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

import java.util.Iterator;
import java.util.LinkedHashSet;

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
    private final Setting<SilentOn> silentOn = register(new EnumSetting<>("Silent On", SilentOn.TICK)).setVisibility(() -> autoSwitch.getValue() == Switch.SILENT);
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
        if(tool == -1) tool = MC.player.inventory.currentItem;
        breakProgress = Math.min(breakProgress + SelfUtils.calcBlockBreakingDelta(MC.world.getBlockState(currentPos), tool), 1);
    }

    private boolean nullCheck() {
        if(MC.player == null || MC.world == null || MC.playerController == null) return true;
        return posQueue.isEmpty() && currentPos == null;
    }

    private void doSwitch() {
        if(hasSwapped && oldSelection != -1) return;

        oldSelection = MC.player.inventory.currentItem;
        toolSlot = InventoryUtils.getTool(currentPos);

        if(autoSwitch.getValue() == Switch.SILENT && progressReady()) {
            if(oldSelection != toolSlot && toolSlot != -1) {
                HOTBAR_TRACKER.setSlot(toolSlot, true, -1);
                HOTBAR_TRACKER.sendSlot();
                hasSwapped = true;
            }
        }

        else if(autoSwitch.getValue() == Switch.NORMAL) {
            if(oldSelection != toolSlot && toolSlot != -1) {
                HOTBAR_TRACKER.setSlot(toolSlot, false, oldSelection);
                hasSwapped = true;
            }
        }

        if(mode.getValue() == Mode.NORMAL) nextTick = true;
    }

    private void doSwitchBack() {
        if(hasSwapped && oldSelection != -1) {
            HOTBAR_TRACKER.reset();
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
        float reachSq = MC.playerController.getBlockReachDistance() *MC.playerController.getBlockReachDistance();

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
        if(MC.world.getBlockState(currentPos).getBlock() instanceof BlockAir
                || MC.world.getBlockState(currentPos).getBlock() instanceof BlockLiquid
                || !WorldUtils.canBreakBlock(currentPos)
        ) {
            currentPos = null;
            hasMined = false;
            return true;
        } else return false;
    }


    private void mine() {
        Pair<EnumFacing, Vec3d> closestVisibleSide = WorldUtils.getClosestVisibleSide(SelfUtils.getEyePos(), currentPos);
        EnumFacing side = null;
        if(closestVisibleSide != null) side = closestVisibleSide.getFirst();
        if(side == null) {
            if(SelfUtils.getEyeY() > currentPos.getY()) side = EnumFacing.UP;
            else side = EnumFacing.DOWN;
        }

        if((!hasMined && mode.getValue() == Mode.NORMAL) || (spam.getValue() && mode.getValue() == Mode.NORMAL)) {
            MC.player.connection.sendPacket(new CPacketPlayerDigging(CPacketPlayerDigging.Action.START_DESTROY_BLOCK, currentPos, side));
            MC.player.connection.sendPacket(new CPacketPlayerDigging(CPacketPlayerDigging.Action.STOP_DESTROY_BLOCK, currentPos, side));
        }

        if(mode.getValue() == Mode.UPDATE) {
            if(clientUpdate.getValue()) {
                if(autoSwitch.getValue() == Switch.SILENT) onPlayerDamageBlock(currentPos, side);
                else MC.playerController.onPlayerDamageBlock(currentPos, side);
            } else {
                if(!hasMined) {
                    breakProgress = 0;
                    MC.player.connection.sendPacket(new CPacketPlayerDigging(CPacketPlayerDigging.Action.START_DESTROY_BLOCK, currentPos, side));
                    hasFinished = false;
                }
                else if(progressReady() && !hasFinished) {
                    MC.player.connection.sendPacket(new CPacketPlayerDigging(CPacketPlayerDigging.Action.STOP_DESTROY_BLOCK, currentPos, side));
                    hasFinished = true;
                }
            }
        }

        if(!hasMined || (spamSwing.getValue() && mode.getValue() == Mode.UPDATE)) {
            if(swing.getValue() == Swing.FULL) MC.player.swingArm(EnumHand.MAIN_HAND);
            if(swing.getValue() == Swing.PACKET) MC.player.connection.sendPacket(new CPacketAnimation(EnumHand.MAIN_HAND));
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
            if(currentPos.equals(event.getBlockPos()) && hasMined && (mode.getValue() == Mode.NORMAL || (mode.getValue() == Mode.UPDATE && !clientUpdate.getValue()))) {
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
    private final EventListener<PacketEvent.Sent> onPacketSent = new EventListener<>(event -> {
        if(event.getPacket() instanceof CPacketPlayerDigging) {
            CPacketPlayerDigging packet = (CPacketPlayerDigging) event.getPacket();

            // Prevent stray sent packets from causing the PacketMine target to fail (more often specifically on update mode)
            if(currentPos != null && packet.getAction() == CPacketPlayerDigging.Action.START_DESTROY_BLOCK)
                if(!packet.getPosition().equals(currentPos))
                    event.setCancelled(true);

            if(!getEnabled()) return;

            if(packet.getAction() == CPacketPlayerDigging.Action.ABORT_DESTROY_BLOCK)
                event.setCancelled(true);
        }
    });

    @EventHandler
    private final EventListener<PacketEvent.Receive> onBlockUpdate = new EventListener<>(event -> {
        if(event.getPacket() instanceof SPacketBlockChange) {
            SPacketBlockChange p = (SPacketBlockChange)event.getPacket();
            if(currentPos == null) return;
            if(p.getBlockState().getBlock() != Blocks.AIR) return;

            if(currentPos.equals(p.getBlockPosition())) {
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
                if(blockPos.equals(p.getBlockPosition())) {
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
                AxisAlignedBB bb = RenderUtils.getBoundingBox(p);
                if(bb != null) RenderUtils.cube(bb, fill, line);
            }
        }

        if(currentPos != null) {
            AxisAlignedBB bb = RenderUtils.getBoundingBox(currentPos);
            if(bb != null) RenderUtils.cube(bb.shrink((1 - breakProgress) /2), cFill, cLine);
        }

        RenderUtils.end3d();
    }

    public boolean onPlayerDamageBlock(BlockPos posBlock, EnumFacing directionFacing)
    {
        if(!hasSwapped || toolSlot == -1) ((PlayerControllerMPAccessor) MC.playerController).syncCurrentPlayItem();
        else MC.player.connection.sendPacket(new CPacketHeldItemChange(toolSlot));
        if(((PlayerControllerMPAccessor) MC.playerController).getBlockHitDelay() > 0) {
            ((PlayerControllerMPAccessor) MC.playerController).setBlockHitDelay(((PlayerControllerMPAccessor) MC.playerController).getBlockHitDelay() -1);
            return true;
        } else if(MC.playerController.getCurrentGameType().isCreative() && MC.world.getWorldBorder().contains(posBlock)) {
            ((PlayerControllerMPAccessor) MC.playerController).setBlockHitDelay(5);
            MC.getTutorial().onHitBlock(MC.world, posBlock, MC.world.getBlockState(posBlock), 1.0F);
            MC.player.connection.sendPacket(new CPacketPlayerDigging(CPacketPlayerDigging.Action.START_DESTROY_BLOCK, posBlock, directionFacing));
            PlayerControllerMP.clickBlockCreative(MC, MC.playerController, posBlock, directionFacing);
            return true;
        } else if(((PlayerControllerMPAccessor) MC.playerController).isHittingPosition(posBlock)) {
            IBlockState iblockstate = MC.world.getBlockState(posBlock);
            Block block = iblockstate.getBlock();
            if(iblockstate.getMaterial() == Material.AIR) {
                ((PlayerControllerMPAccessor) MC.playerController).setIsHittingBlock(false);
                return false;
            } else {
                ((PlayerControllerMPAccessor) MC.playerController).setCurBlockDamageMP(breakProgress);
                if(((PlayerControllerMPAccessor) MC.playerController).getStepSoundTickCounter() % 4.0F == 0.0F) {
                    SoundType soundtype = block.getSoundType(iblockstate, MC.world, posBlock, MC.player);
                    MC.getSoundHandler().playSound(new PositionedSoundRecord(soundtype.getHitSound(), SoundCategory.NEUTRAL, (soundtype.getVolume() + 1.0F) / 8.0F, soundtype.getPitch() * 0.5F, posBlock));
                }

                ((PlayerControllerMPAccessor) MC.playerController).setStepSoundTickCounter(((PlayerControllerMPAccessor) MC.playerController).getStepSoundTickCounter() +1);
                MC.getTutorial().onHitBlock(MC.world, posBlock, iblockstate, MathHelper.clamp(((PlayerControllerMPAccessor) MC.playerController).getCurBlockDamageMP(), 0.0F, 1.0F));
                if(((PlayerControllerMPAccessor) MC.playerController).getCurBlockDamageMP() >= 1.0F) {
                    ((PlayerControllerMPAccessor) MC.playerController).setIsHittingBlock(false);
                    MC.player.connection.sendPacket(new CPacketPlayerDigging(CPacketPlayerDigging.Action.STOP_DESTROY_BLOCK, posBlock, directionFacing));
                    MC.playerController.onPlayerDestroyBlock(posBlock);
                    ((PlayerControllerMPAccessor) MC.playerController).setCurBlockDamageMP(0.0F);
                    ((PlayerControllerMPAccessor) MC.playerController).setStepSoundTickCounter(0.0F);
                    ((PlayerControllerMPAccessor) MC.playerController).setBlockHitDelay(5);
                }

                MC.world.sendBlockBreakProgress(MC.player.getEntityId(), ((PlayerControllerMPAccessor) MC.playerController).getCurrentBlock(), (int)(((PlayerControllerMPAccessor) MC.playerController).getCurBlockDamageMP() * 10.0F) - 1);
                return true;
            }
        }
        else return MC.playerController.clickBlock(posBlock, directionFacing);
    }
}
