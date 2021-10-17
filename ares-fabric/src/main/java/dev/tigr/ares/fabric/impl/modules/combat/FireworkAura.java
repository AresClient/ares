package dev.tigr.ares.fabric.impl.modules.combat;

import dev.tigr.ares.core.feature.module.Category;
import dev.tigr.ares.core.feature.module.Module;
import dev.tigr.ares.core.setting.Setting;
import dev.tigr.ares.core.setting.settings.BooleanSetting;
import dev.tigr.ares.core.setting.settings.numerical.DoubleSetting;
import dev.tigr.ares.core.setting.settings.numerical.IntegerSetting;
import dev.tigr.ares.core.util.Priorities;
import dev.tigr.ares.core.util.Timer;
import dev.tigr.ares.core.util.render.TextColor;
import dev.tigr.ares.fabric.utils.InventoryUtils;
import dev.tigr.ares.fabric.utils.WorldUtils;
import dev.tigr.ares.fabric.utils.entity.PlayerUtils;
import dev.tigr.ares.fabric.utils.entity.SelfUtils;
import net.minecraft.entity.Entity;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

import static dev.tigr.ares.fabric.impl.modules.player.RotationManager.ROTATIONS;

/**
 * @author Makrennel
 */
@Module.Info(name = "FireworkAura", description = "Automatically traps above head and places fireworks on other people.", category = Category.COMBAT)
public class FireworkAura extends Module {

    private final Setting<Integer> delay = register(new IntegerSetting("Delay", 1, 0, 20));
    private final Setting<Integer> trapDelay = register(new IntegerSetting("Trap", 2, 0, 20));
    private final Setting<Double> range = register(new DoubleSetting("Range", 5, 0, 8));
    private final Setting<Boolean> rotate = register(new BooleanSetting("Rotate", true));

    private Timer trapTimer = new Timer();
    private Timer delayTimer = new Timer();

    int key = Priorities.Rotation.FIREWORK_AURA;

    @Override
    public void onDisable() {
        ROTATIONS.setCompletedAction(key, true);
    }

    @Override
    public void onTick() {
        // get targets
        for(Entity playerEntity : WorldUtils.getPlayerTargets(range.getValue())) {
            BlockPos playerPos = playerEntity.getBlockPos();
            BlockPos trapPos = new BlockPos(playerPos.getX(), playerPos.getY() + 2, playerPos.getZ());

            // place trap
            if(MC.world.getBlockState(trapPos).getMaterial().isReplaceable()) {
                if(trapTimer.passedTicks(trapDelay.getValue())) {
                    int oldSelection = MC.player.getInventory().selectedSlot;
                    int newSelection = InventoryUtils.getBlockInHotbar();
                    if(newSelection == -1) {
                        setEnabled(false);
                        UTILS.printMessage(TextColor.RED + "No Blocks Found");
                    } else MC.player.getInventory().selectedSlot = newSelection;
                    SelfUtils.placeBlockMainHand(rotate.getValue(), key, key, false, false, trapPos);
                    MC.player.getInventory().selectedSlot = oldSelection;
                    delayTimer.reset();
                    trapTimer.reset();
                }
            }

            // place fireworks
            else if(delayTimer.passedTicks(delay.getValue())) {
                if(Math.sqrt(MC.player.squaredDistanceTo(playerPos.getX(), playerPos.getY(), playerPos.getZ())) <= range.getValue()) {

                    // switch
                    int oldSelection = MC.player.getInventory().selectedSlot;
                    int newSelection = InventoryUtils.findItemInHotbar(Items.FIREWORK_ROCKET);
                    if(newSelection == -1) return;
                    else MC.player.getInventory().selectedSlot = newSelection;

                    // rotate
                    if(rotate.getValue()) {
                        double[] rotations = PlayerUtils.calculateLookFromPlayer(playerPos.getX() + 0.5, playerPos.getY(), playerPos.getZ() + 0.5, MC.player);
                        ROTATIONS.setCurrentRotation((float) rotations[0], (float) rotations[1], key, key, false, false);
                    }
                    // place
                    MC.player.networkHandler.sendPacket(new ClientCommandC2SPacket(MC.player, ClientCommandC2SPacket.Mode.PRESS_SHIFT_KEY));
                    MC.interactionManager.interactBlock(MC.player, MC.world, Hand.MAIN_HAND, new BlockHitResult(new Vec3d(playerPos.getX() + 0.5, playerPos.getY(), playerPos.getZ() + 0.5), Direction.UP, playerPos, false));
                    MC.player.swingHand(Hand.MAIN_HAND);
                    MC.player.networkHandler.sendPacket(new ClientCommandC2SPacket(MC.player, ClientCommandC2SPacket.Mode.RELEASE_SHIFT_KEY));

                    // switch return
                    MC.player.getInventory().selectedSlot = oldSelection;

                    // reset timer
                    trapTimer.reset();
                    delayTimer.reset();
                }
            }
        }
    }
}
