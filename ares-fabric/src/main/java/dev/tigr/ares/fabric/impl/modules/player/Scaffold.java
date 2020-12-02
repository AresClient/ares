package dev.tigr.ares.fabric.impl.modules.player;

import dev.tigr.ares.core.feature.module.Category;
import dev.tigr.ares.core.feature.module.Module;
import dev.tigr.ares.core.setting.Setting;
import dev.tigr.ares.core.setting.settings.BooleanSetting;
import dev.tigr.ares.core.setting.settings.numerical.IntegerSetting;
import dev.tigr.ares.core.util.render.TextColor;
import dev.tigr.ares.fabric.event.movement.WalkOffLedgeEvent;
import dev.tigr.ares.fabric.utils.InventoryUtils;
import dev.tigr.ares.fabric.utils.WorldUtils;
import dev.tigr.simpleevents.listener.EventHandler;
import dev.tigr.simpleevents.listener.EventListener;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;

import java.util.ArrayList;

/**
 * @author Tigermouthbear
 * ported to Fabric by Hoosiers
 */
@Module.Info(name = "Scaffold", description = "Automatically bridges for you", category = Category.PLAYER)
public class Scaffold extends Module {
    private final Setting<Integer> radius = register(new IntegerSetting("Radius", 0, 0, 2));
    private final Setting<Boolean> down = register(new BooleanSetting("Down", false));
    @EventHandler
    public EventListener<WalkOffLedgeEvent> walkOffLedgeEvent = new EventListener<>(event -> {
        if(!down.getValue() && !MC.player.isSprinting()) event.isSneaking = true;
    });

    @Override
    public void onMotion() {
        int oldSlot = MC.player.inventory.selectedSlot;
        int newSlot = InventoryUtils.getBlockInHotbar();

        if(newSlot != -1) {
            MC.player.inventory.selectedSlot = newSlot;
        } else {
            UTILS.printMessage(TextColor.RED + "No blocks found in hotbar!");
            setEnabled(false);
            return;
        }

        //Down
        if(radius.getValue() != 0 && down.getValue()) radius.setValue(0);

        if(MC.options.keySprint.isPressed() && down.getValue()) {
            float yaw = (float) Math.toRadians(MC.player.yaw);
            double yVelocity = MC.player.getVelocity().y;

            if(MC.options.keyForward.isPressed()) {
                MC.player.setVelocity(-MathHelper.cos(yaw) * 0.03f, yVelocity, MathHelper.sin(yaw) * 0.03f);
            }
            if(MC.options.keyBack.isPressed()) {
                MC.player.setVelocity(MathHelper.cos(yaw) * 0.03f, yVelocity, -MathHelper.sin(yaw) * 0.03f);
            }
            if(MC.options.keyLeft.isPressed()) {
                MC.player.setVelocity(MathHelper.cos(yaw) * 0.03f, yVelocity, MathHelper.sin(yaw) * 0.03f);
            }
            if(MC.options.keyRight.isPressed()) {
                MC.player.setVelocity(-MathHelper.cos(yaw) * 0.03f, yVelocity, -MathHelper.sin(yaw) * 0.03f);
            }

            BlockPos under = new BlockPos(MC.player.getX(), MC.player.getY() - 2, MC.player.getZ());

            if(MC.world.getBlockState(under).getMaterial().isReplaceable()) WorldUtils.placeBlockMainHand(under);

            MC.player.inventory.selectedSlot = oldSlot;

            return;
        }

        //Radius = 0
        if(radius.getValue() == 0) {
            BlockPos under = new BlockPos(MC.player.getX(), MC.player.getY() - 1, MC.player.getZ());

            if(MC.world.getBlockState(under).getMaterial().isReplaceable()) WorldUtils.placeBlockMainHand(under);

            MC.player.inventory.selectedSlot = oldSlot;

            return;
        }

        //Radius > 0
        ArrayList<BlockPos> blocks = new ArrayList<>();
        for(int x = -radius.getValue(); x <= radius.getValue(); x++) {
            for(int z = -radius.getValue(); z <= radius.getValue(); z++) {
                blocks.add(new BlockPos(MC.player.getX() + x, MC.player.getY() - 1, MC.player.getZ() + z));
            }
        }

        for(BlockPos x: blocks) {
            if(MC.world.getBlockState(x).getMaterial().isReplaceable()) {
                WorldUtils.placeBlockMainHand(x);
                break;
            }
        }

        MC.player.inventory.selectedSlot = oldSlot;
    }
}
