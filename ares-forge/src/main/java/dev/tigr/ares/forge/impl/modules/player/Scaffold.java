package dev.tigr.ares.forge.impl.modules.player;

import dev.tigr.ares.core.feature.module.Category;
import dev.tigr.ares.core.feature.module.Module;
import dev.tigr.ares.core.setting.Setting;
import dev.tigr.ares.core.setting.settings.BooleanSetting;
import dev.tigr.ares.core.setting.settings.numerical.IntegerSetting;
import dev.tigr.ares.core.util.render.TextColor;
import dev.tigr.ares.forge.event.events.movement.WalkOffLedgeEvent;
import dev.tigr.ares.forge.utils.InventoryUtils;
import dev.tigr.ares.forge.utils.WorldUtils;
import dev.tigr.simpleevents.listener.EventHandler;
import dev.tigr.simpleevents.listener.EventListener;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;

import java.util.ArrayList;

/**
 * @author Tigermouthbear
 */
@Module.Info(name = "Scaffold", description = "Automatically bridges for you", category = Category.PLAYER)
public class Scaffold extends Module {
    private final Setting<Integer> radius = register(new IntegerSetting("Radius", 0, 0, 2));
    private final Setting<Boolean> rotate = register(new BooleanSetting("Rotate", true));
    private final Setting<Boolean> down = register(new BooleanSetting("Down", false));
    @EventHandler
    public EventListener<WalkOffLedgeEvent> walkOffLedgeEvent = new EventListener<>(event -> {
        if(!down.getValue() && !MC.player.isSprinting()) event.isSneaking = true;
    });

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
