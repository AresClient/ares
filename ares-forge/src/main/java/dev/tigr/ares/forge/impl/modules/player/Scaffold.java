package dev.tigr.ares.forge.impl.modules.player;

import dev.tigr.ares.core.feature.module.Category;
import dev.tigr.ares.core.feature.module.Module;
import dev.tigr.ares.core.setting.Setting;
import dev.tigr.ares.core.setting.settings.BooleanSetting;
import dev.tigr.ares.core.setting.settings.numerical.IntegerSetting;
import dev.tigr.ares.core.util.Priorities;
import dev.tigr.ares.core.util.Timer;
import dev.tigr.ares.core.util.render.TextColor;
import dev.tigr.ares.forge.event.events.movement.PlayerJumpEvent;
import dev.tigr.ares.forge.event.events.movement.WalkOffLedgeEvent;
import dev.tigr.ares.forge.utils.InventoryUtils;
import dev.tigr.ares.forge.utils.WorldUtils;
import dev.tigr.ares.forge.utils.entity.SelfUtils;
import dev.tigr.simpleevents.listener.EventHandler;
import dev.tigr.simpleevents.listener.EventListener;
import net.minecraft.block.BlockAir;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;

import java.util.ArrayList;

import static dev.tigr.ares.forge.impl.modules.player.RotationManager.ROTATIONS;

/**
 * @author Tigermouthbear
 * Tower added by Makrennel 3/31/21
 */
@Module.Info(name = "Scaffold", description = "Automatically bridges for you", category = Category.PLAYER)
public class Scaffold extends Module {
    private final Setting<Integer> radius = register(new IntegerSetting("Radius", 0, 0, 6));
    private final Setting<Boolean> rotate = register(new BooleanSetting("Rotate", true));
    private final Setting<Boolean> down = register(new BooleanSetting("Down", false));
    private final Setting<Boolean> airplace = register(new BooleanSetting("AirPlace", false));
    private final Setting<Boolean> tower = register(new BooleanSetting("Tower", true)).setVisibility(() -> radius.getValue() <= 0);
    private final Setting<Boolean> packetTower = register(new BooleanSetting("Packet Tower", false)).setVisibility(() -> tower.getValue() && radius.getValue() <= 0);
    private final Setting<Integer> towerJumpVelocity = register(new IntegerSetting("Jump Velocity", 42, 37, 60)).setVisibility(() -> tower.getValue() && radius.getValue() <= 0 && !packetTower.getValue());
    private final Setting<Integer> towerReturnVelocity = register(new IntegerSetting("Return Velocity", 20, 0, 40)).setVisibility(() -> tower.getValue() && radius.getValue() <= 0 && !packetTower.getValue());
    private final Setting<Integer> towerClipDelay = register(new IntegerSetting("Clip Delay", 128, 1, 500)).setVisibility(() -> tower.getValue() && radius.getValue() <= 0 && packetTower.getValue());
    private final Setting<Boolean> towerReturnPacket = register(new BooleanSetting("Packet Return To Ground", false)).setVisibility(() -> tower.getValue() && radius.getValue() <= 0 && packetTower.getValue());
    private final Setting<Integer> returnDelay = register(new IntegerSetting("Return Delay", 7, 0, 12)).setVisibility(() -> tower.getValue() && radius.getValue() <= 0 && packetTower.getValue() && towerReturnPacket.getValue());

    private final Timer towerDelayTimer = new Timer();
    private boolean shouldResetTower;

    int key = Priorities.Rotation.SCAFFOLD;

    @EventHandler
    public EventListener<WalkOffLedgeEvent> walkOffLedgeEvent = new EventListener<>(event -> {
        if(MC.player == null || MC.world == null || (down.getValue() && MC.player.isSprinting())) return;
        event.setCancelled(true);
    });

    @EventHandler
    public EventListener<PlayerJumpEvent> onPlayerJumpEvent = new EventListener<>(event -> {
        if(tower.getValue() && radius.getValue() <= 0) {
            event.setCancelled(true);
        }
    });

    @Override
    public void onDisable() {
        ROTATIONS.setCompletedAction(key, true);
    }

    @Override
    public void onTick() {
        if(MC.player == null || MC.world == null) return;

        //Release Rotations
        if(rotate.getValue()) {
            boolean flagCurrent = true;
            if(MC.gameSettings.keyBindSprint.isPressed() && down.getValue()) {
                BlockPos under = new BlockPos(MC.player.posX, MC.player.posY - 2, MC.player.posZ);

                if (MC.world.getBlockState(under).getMaterial().isReplaceable())
                    flagCurrent = false;
            }

            if(radius.getValue() == 0) {
                BlockPos under = new BlockPos(MC.player.posX, MC.player.posY - 1, MC.player.posZ);

                if (MC.world.getBlockState(under).getMaterial().isReplaceable())
                    flagCurrent = false;
            }

            if(radius.getValue() > 0) {
                ArrayList<BlockPos> blocks = new ArrayList<>();
                for (int x = -radius.getValue(); x <= radius.getValue(); x++) {
                    for (int z = -radius.getValue(); z <= radius.getValue(); z++) {
                        blocks.add(new BlockPos(MC.player.posX + x, MC.player.posY - 1, MC.player.posZ + z));
                    }
                }

                for (BlockPos x : blocks) {
                    if (MC.world.getBlockState(x).getMaterial().isReplaceable())
                        flagCurrent = false;
                }
            }

            if(ROTATIONS.isKeyCurrent(key) && flagCurrent) ROTATIONS.setCompletedAction(key, true);
        }

        if(tower.getValue() && MC.gameSettings.keyBindJump.isKeyDown() && radius.getValue() <= 0 && !packetTower.getValue()) {
            if(MC.player.onGround)
                MC.player.setVelocity(MC.player.motionX *0.3,towerJumpVelocity.getValue().doubleValue() /100, MC.player.motionZ *0.3);
            if(MC.world.getBlockState(new BlockPos(MC.player.posX, Math.floor(MC.player.posY) -1, MC.player.posZ)).getBlock() instanceof BlockAir)
                MC.player.setVelocity(MC.player.motionX *0.3, -(towerReturnVelocity.getValue().doubleValue() /100), MC.player.motionZ *0.3);
        }
        if(tower.getValue() && packetTower.getValue() && MC.gameSettings.keyBindJump.isKeyDown() && radius.getValue() <= 0) {
            if(shouldResetTower) {
                towerDelayTimer.reset();
                shouldResetTower = false;
            }
            if(!MC.player.onGround) {
                if (!(MC.world.getBlockState(WorldUtils.roundBlockPos(MC.player.getPositionVector()).down()).getBlock() instanceof BlockAir)
                        && towerReturnPacket.getValue()
                        && towerDelayTimer.passedMillis(returnDelay.getValue())) {
                    MC.player.connection.sendPacket(new CPacketPlayer.Position(MC.player.posX, Math.floor(MC.player.posY), MC.player.posZ, true));
                    MC.player.setPosition(MC.player.posX, Math.floor(MC.player.posY), MC.player.posZ);
                }
                shouldResetTower = true;
                return;
            }
            if(towerDelayTimer.passedMillis(towerClipDelay.getValue())) {
                // Pretend that we are jumping to the server and then update player position to meet where the server thinks the player is instantly.
                SelfUtils.fakeJump(1,4);
                MC.player.setPosition(MC.player.posX, MC.player.posY + 1.15, MC.player.posZ);
                shouldResetTower = true;
            }
        }
    }

    @Override
    public void onMotion() {
        if(MC.player == null || MC.world == null) return;

        int oldSlot = MC.player.inventory.currentItem;
        int newSlot = InventoryUtils.getBlockInHotbar();

        if(newSlot != -1) {
            MC.player.inventory.currentItem = newSlot;
        } else {
            UTILS.printMessage(TextColor.RED + "No blocks found in hotbar!");
            setEnabled(false);
            return;
        }

        //Down
        if(radius.getValue() != 0 && down.getValue()) radius.setValue(0);

        if(MC.gameSettings.keyBindSprint.isKeyDown() && down.getValue()) {
            float yaw = (float) Math.toRadians(MC.player.rotationYaw);

            if(MC.gameSettings.keyBindForward.isKeyDown()) {
                MC.player.motionX = -MathHelper.sin(yaw) * 0.03f;
                MC.player.motionZ = MathHelper.cos(yaw) * 0.03f;
            }
            if(MC.gameSettings.keyBindBack.isKeyDown()) {
                MC.player.motionX = MathHelper.sin(yaw) * 0.03f;
                MC.player.motionZ = -MathHelper.cos(yaw) * 0.03f;
            }
            if(MC.gameSettings.keyBindLeft.isKeyDown()) {
                MC.player.motionX = MathHelper.cos(yaw) * 0.03f;
                MC.player.motionZ = MathHelper.sin(yaw) * 0.03f;
            }
            if(MC.gameSettings.keyBindRight.isKeyDown()) {
                MC.player.motionX = -MathHelper.cos(yaw) * 0.03f;
                MC.player.motionZ = -MathHelper.sin(yaw) * 0.03f;
            }

            BlockPos under = new BlockPos(MC.player.posX, MC.player.posY - 2, MC.player.posZ);

            if(MC.world.getBlockState(under).getMaterial().isReplaceable())
                SelfUtils.placeBlockMainHand(rotate.getValue(), key, key, false, false, under, airplace.getValue());

            MC.player.inventory.currentItem = oldSlot;

            return;
        }

        //Radius = 0
        if(radius.getValue() == 0) {
            BlockPos under = new BlockPos(MC.player.posX, MC.player.posY - 1, MC.player.posZ);

            if(MC.world.getBlockState(under).getMaterial().isReplaceable())
                SelfUtils.placeBlockMainHand(rotate.getValue(), key, key, false, false, under, airplace.getValue());

            MC.player.inventory.currentItem = oldSlot;

            return;
        }

        //Radius > 0
        ArrayList<BlockPos> blocks = new ArrayList<>();
        for(int x = -radius.getValue(); x <= radius.getValue(); x++) {
            for(int z = -radius.getValue(); z <= radius.getValue(); z++) {
                blocks.add(new BlockPos(MC.player.posX + x, MC.player.posY - 1, MC.player.posZ + z));
            }
        }

        for(BlockPos x: blocks) {
            if(MC.world.getBlockState(x).getMaterial().isReplaceable()) {
                SelfUtils.placeBlockMainHand(rotate.getValue(), key, key, false, false, x, airplace.getValue());
                break;
            }
        }

        MC.player.inventory.currentItem = oldSlot;
    }
}
