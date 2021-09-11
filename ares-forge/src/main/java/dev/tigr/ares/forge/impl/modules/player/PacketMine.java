package dev.tigr.ares.forge.impl.modules.player;

import dev.tigr.ares.core.event.client.PostInitializationEvent;
import dev.tigr.ares.core.feature.module.Category;
import dev.tigr.ares.core.feature.module.Module;
import dev.tigr.ares.core.setting.Setting;
import dev.tigr.ares.core.setting.settings.BooleanSetting;
import dev.tigr.ares.core.setting.settings.EnumSetting;
import dev.tigr.ares.core.setting.settings.numerical.FloatSetting;
import dev.tigr.ares.core.util.render.Color;
import dev.tigr.ares.forge.event.events.player.DamageBlockEvent;
import dev.tigr.ares.forge.utils.WorldUtils;
import dev.tigr.ares.forge.utils.render.RenderUtils;
import dev.tigr.simpleevents.listener.EventHandler;
import dev.tigr.simpleevents.listener.EventListener;
import net.minecraft.block.BlockAir;
import net.minecraft.block.BlockLiquid;
import net.minecraft.network.play.client.CPacketAnimation;
import net.minecraft.network.play.client.CPacketPlayerDigging;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumFacing;

import java.util.Iterator;
import java.util.LinkedHashSet;

/***
 * @author Makrennel 08/09/21
 * A module for managing packet mining
 */
@Module.Info(name = "PacketMine", description = "Mines using packets", category = Category.PLAYER, alwaysListening = true)
public class PacketMine extends Module {
    public PacketMine INSTANCE;

    private final Setting<Mode> mode = register(new EnumSetting<>("Mode", Mode.NORMAL));
    private final Setting<Swing> swing = register(new EnumSetting<>("Swing Type", Swing.FULL));
    private final Setting<Boolean> swingComplete = register(new BooleanSetting("Swing Till Done", true)).setVisibility(() -> mode.getValue() == Mode.UPDATE && swing.getValue() != Swing.NONE);

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

    public PacketMine() {
        INSTANCE = this;
    }

    private LinkedHashSet<BlockPos> posQueue = new LinkedHashSet<>();
    private BlockPos currentPos;
    private boolean hasMined;

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
        if(nullCheck()) return;

        // Check if currentPos is null and get the next queue element if it is
        pollQueueIfNull();

        // Check if currentPos is still a breakable block and remove from the queue if it no longer is
        if(checkAndClearCurrent()) return;

        // Remove all targets out of distance
        if(removeOutOfDistance()) return;

        // Send Start/Stop mining packets if mode is Normal
        if(mode.getValue() == Mode.NORMAL && !hasMined) {
            normalMine();
        }

        // Perform update block function if mode is Update
        if(mode.getValue() == Mode.UPDATE) {
            updateMine();
        }
    }

    private boolean nullCheck() {
        if(MC.player == null || MC.world == null || MC.playerController == null) return true;
        return posQueue.isEmpty() && currentPos == null;
    }

    private boolean removeOutOfDistance() {
        float reachSq = MC.playerController.getBlockReachDistance() *MC.playerController.getBlockReachDistance();

        posQueue.removeIf(p -> MC.player.getDistanceSq(p.getX() + 0.5, p.getY() + 0.5, p.getZ() + 0.5) > reachSq);

        if(MC.player.getDistanceSq(currentPos.getX() + 0.5, currentPos.getY() + 0.5, currentPos.getZ() + 0.5) > reachSq) {
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

    private void normalMine() {
        MC.player.connection.sendPacket(new CPacketPlayerDigging(CPacketPlayerDigging.Action.START_DESTROY_BLOCK, currentPos, EnumFacing.UP));

        if(swing.getValue() == Swing.FULL) MC.player.swingArm(EnumHand.MAIN_HAND);
        if(swing.getValue() == Swing.PACKET) MC.player.connection.sendPacket(new CPacketAnimation(EnumHand.MAIN_HAND));

        MC.player.connection.sendPacket(new CPacketPlayerDigging(CPacketPlayerDigging.Action.STOP_DESTROY_BLOCK, currentPos, EnumFacing.UP));

        hasMined = true;
    }

    private void updateMine() {
        MC.playerController.onPlayerDamageBlock(currentPos, EnumFacing.UP);

        if(swingComplete.getValue() || !hasMined) {
            if(swing.getValue() == Swing.FULL) MC.player.swingArm(EnumHand.MAIN_HAND);
            if(swing.getValue() == Swing.PACKET) MC.player.connection.sendPacket(new CPacketAnimation(EnumHand.MAIN_HAND));
        }

        hasMined = true;
    }

    @EventHandler
    private final EventListener<DamageBlockEvent> onDamageBlock = new EventListener<>(event -> {
        if(!getEnabled() || currentPos == event.getBlockPos() || !WorldUtils.canBreakBlock(event.getBlockPos())) return;

        addPos(event.getBlockPos());
        event.setCancelled(true);
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

        AxisAlignedBB bb = RenderUtils.getBoundingBox(currentPos);
        if(bb != null) RenderUtils.cube(bb, cFill, cLine);

        RenderUtils.end3d();
    }

}
