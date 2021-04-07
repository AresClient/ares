package dev.tigr.ares.forge.impl.modules.player;

import dev.tigr.ares.core.feature.module.Category;
import dev.tigr.ares.core.feature.module.Module;
import dev.tigr.ares.core.setting.Setting;
import dev.tigr.ares.core.setting.settings.BooleanSetting;
import dev.tigr.ares.core.setting.settings.numerical.IntegerSetting;
import dev.tigr.ares.core.util.render.TextColor;
import dev.tigr.ares.forge.event.events.movement.PlayerJumpEvent;
import dev.tigr.ares.forge.event.events.movement.WalkOffLedgeEvent;
import dev.tigr.ares.forge.utils.InventoryUtils;
import dev.tigr.ares.forge.utils.Timer;
import dev.tigr.ares.forge.utils.WorldUtils;
import dev.tigr.simpleevents.listener.EventHandler;
import dev.tigr.simpleevents.listener.EventListener;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;

import java.util.ArrayList;

/**
 * @author Tigermouthbear
 * Tower added by Makrennel 3/31/21
 */
@Module.Info(name = "Scaffold", description = "Automatically bridges for you", category = Category.PLAYER)
public class Scaffold extends Module {
    private final Setting<Integer> radius = register(new IntegerSetting("Radius", 0, 0, 6));
    private final Setting<Boolean> rotate = register(new BooleanSetting("Rotate", true));
    private final Setting<Boolean> down = register(new BooleanSetting("Down", false));
    private final Setting<Boolean> tower = register(new BooleanSetting("Tower", true)).setVisibility(() -> radius.getValue() <= 0);
    private final Setting<Integer> towerDelay = register(new IntegerSetting("Clip Delay", 128, 1, 500)).setVisibility(() -> tower.getValue() && radius.getValue() <= 0);

    private Timer towerDelayTimer = new Timer();

    @EventHandler
    public EventListener<WalkOffLedgeEvent> walkOffLedgeEvent = new EventListener<>(event -> {
        if(!down.getValue() && !MC.player.isSprinting()) event.isSneaking = true;
    });

    @EventHandler
    public EventListener<PlayerJumpEvent> onPlayerJumpEvent = new EventListener<>(event -> {
        if(tower.getValue() && radius.getValue() <= 0) {
            event.setCancelled(true);
        }
    });

    @Override
    public void onTick() {
        if(tower.getValue() && MC.gameSettings.keyBindJump.isKeyDown() && radius.getValue() <= 0) {
            if(towerDelayTimer.passedMillis(towerDelay.getValue())) {
                // Pretend that we are jumping to the server and then update player position to meet where the server thinks the player is instantly.
                WorldUtils.fakeJump();
                MC.player.setPosition(MC.player.posX, MC.player.posY + 1.15, MC.player.posZ);
                // It may be preferable to make the value configurable for this, but from what I found in my (limited) testing +7 works best generally with NCP, and any higher than +10ish tends not to work at all
                if(towerDelayTimer.passedMillis(towerDelay.getValue() + 7)) {
                    // Make sure player is on ground before repeating, clips back down if not to speed up the process
                    if(!MC.player.onGround) {
                        if(!MC.world.getBlockState(new BlockPos(MC.player.posX, MC.player.posY - 1, MC.player.posZ)).getMaterial().isReplaceable()) {
                            MC.player.connection.sendPacket(new CPacketPlayer.Position(MC.player.posX, Math.floor(MC.player.posY), MC.player.posZ, true));
                            MC.player.setPosition(MC.player.posX, Math.floor(MC.player.posY), MC.player.posZ);
                        }
                    }
                    towerDelayTimer.reset();
                }
            }
        }
    }

    @Override
    public void onMotion() {
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

            if(MC.world.getBlockState(under).getMaterial().isReplaceable()) WorldUtils.placeBlockMainHand(under, rotate.getValue());

            MC.player.inventory.currentItem = oldSlot;

            return;
        }

        //Radius = 0
        if(radius.getValue() == 0) {
            BlockPos under = new BlockPos(MC.player.posX, MC.player.posY - 1, MC.player.posZ);

            if(MC.world.getBlockState(under).getMaterial().isReplaceable()) WorldUtils.placeBlockMainHand(under, rotate.getValue());

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
                WorldUtils.placeBlockMainHand(x, rotate.getValue());
                break;
            }
        }

        MC.player.inventory.currentItem = oldSlot;
    }
}
