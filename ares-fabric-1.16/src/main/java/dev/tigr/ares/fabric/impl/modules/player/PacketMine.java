package dev.tigr.ares.fabric.impl.modules.player;

import dev.tigr.ares.core.event.client.PostInitializationEvent;
import dev.tigr.ares.core.feature.module.Category;
import dev.tigr.ares.core.feature.module.Module;
import dev.tigr.ares.core.setting.Setting;
import dev.tigr.ares.core.setting.settings.BooleanSetting;
import dev.tigr.ares.core.setting.settings.EnumSetting;
import dev.tigr.ares.core.setting.settings.numerical.FloatSetting;
import dev.tigr.ares.core.util.render.Color;
import dev.tigr.ares.fabric.event.player.DamageBlockEvent;
import dev.tigr.ares.fabric.utils.MathUtils;
import dev.tigr.ares.fabric.utils.WorldUtils;
import dev.tigr.ares.fabric.utils.entity.SelfUtils;
import dev.tigr.ares.fabric.utils.render.RenderUtils;
import dev.tigr.simpleevents.listener.EventHandler;
import dev.tigr.simpleevents.listener.EventListener;
import net.minecraft.block.AirBlock;
import net.minecraft.block.FluidBlock;
import net.minecraft.network.packet.c2s.play.HandSwingC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

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
        if(MC.player == null || MC.world == null || MC.interactionManager == null) return true;
        return posQueue.isEmpty() && currentPos == null;
    }

    private boolean removeOutOfDistance() {
        float reachSq = MC.interactionManager.getReachDistance() *MC.interactionManager.getReachDistance();

        posQueue.removeIf(p -> {
            Vec3d closest = MathUtils.getClosestPointOfBlockPos(SelfUtils.getPlayer().getPos(), p);
            return SelfUtils.getEyePos().squaredDistanceTo(closest.getX(), closest.getY(), closest.getZ()) > reachSq;
        });

        Vec3d closest = MathUtils.getClosestPointOfBlockPos(SelfUtils.getPlayer().getPos(), currentPos);

        if(SelfUtils.getEyePos().squaredDistanceTo(closest.getX(), closest.getY(), closest.getZ()) > reachSq) {
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
        if(MC.world.getBlockState(currentPos).getBlock() instanceof AirBlock
                || MC.world.getBlockState(currentPos).getBlock() instanceof FluidBlock
                || !WorldUtils.canBreakBlock(currentPos)
        ) {
            currentPos = null;
            hasMined = false;
            return true;
        } else return false;
    }

    private void normalMine() {
        MC.player.networkHandler.sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.START_DESTROY_BLOCK, currentPos, Direction.UP));

        if(swing.getValue() == Swing.FULL) MC.player.swingHand(Hand.MAIN_HAND);
        if(swing.getValue() == Swing.PACKET) MC.player.networkHandler.sendPacket(new HandSwingC2SPacket(Hand.MAIN_HAND));

        MC.player.networkHandler.sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.STOP_DESTROY_BLOCK, currentPos, Direction.UP));

        hasMined = true;
    }

    private void updateMine() {
        MC.interactionManager.updateBlockBreakingProgress(currentPos, Direction.UP);

        if(swingComplete.getValue() || !hasMined) {
            if(swing.getValue() == Swing.FULL) MC.player.swingHand(Hand.MAIN_HAND);
            if(swing.getValue() == Swing.PACKET) MC.player.networkHandler.sendPacket(new HandSwingC2SPacket(Hand.MAIN_HAND));
        }

        hasMined = true;
    }

    @EventHandler
    private final EventListener<DamageBlockEvent> onDamageBlock = new EventListener<>(event -> {
        if(!getEnabled() || !WorldUtils.canBreakBlock(event.getBlockPos())) return;

        if(currentPos == event.getBlockPos() && hasMined) {
            normalMine();
            return;
        } else if(currentPos == event.getBlockPos()) return;

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
                Box bb = RenderUtils.getBoundingBox(p);
                if(bb != null) RenderUtils.cube(bb, fill, line);
            }
        }

        if(currentPos != null) {
            Box bb = RenderUtils.getBoundingBox(currentPos);
            if(bb != null) RenderUtils.cube(bb, cFill, cLine);
        }

        RenderUtils.end3d();
    }
}
