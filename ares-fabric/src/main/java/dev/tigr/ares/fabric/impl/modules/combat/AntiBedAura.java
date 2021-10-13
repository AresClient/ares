package dev.tigr.ares.fabric.impl.modules.combat;

import dev.tigr.ares.core.feature.module.Category;
import dev.tigr.ares.core.feature.module.Module;
import dev.tigr.ares.core.setting.Setting;
import dev.tigr.ares.core.setting.settings.BooleanSetting;
import dev.tigr.ares.core.util.Priorities;
import dev.tigr.ares.fabric.utils.HoleType;
import dev.tigr.ares.fabric.utils.InventoryUtils;
import dev.tigr.ares.fabric.utils.WorldUtils;
import net.minecraft.block.Block;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;

import static dev.tigr.ares.fabric.impl.modules.player.RotationManager.ROTATIONS;

/**
 * @author Tigermouthbear
 */
@Module.Info(name = "AntiBedAura", description = "automatically places string in hitbox when in hole to prevent beds", category = Category.COMBAT)
public class AntiBedAura extends Module {
    private final Setting<Boolean> rotate = register(new BooleanSetting("Rotate", true));

    int key = Priorities.Rotation.ANTI_BED_AURA;

    @Override
    public void onDisable() {
        ROTATIONS.setCompletedAction(key, true);
    }

    // TODO: remove this or make it work better
    // Most bedauras no longer trip with string inside the player's hitbox, maybe make this into something which detects beds and automatically triggers burrow or self trap as appropriate, and try to mine beds
    @Override
    public void onTick() {
        if(MC.world.getBlockState(MC.player.getBlockPos().up()).getBlock() == Block.getBlockFromItem(Items.STRING)) {
            if(rotate.getValue()) ROTATIONS.setCompletedAction(key, true);
        }

        if(WorldUtils.isHole(MC.player.getBlockPos()) != HoleType.NONE && MC.player.isOnGround() && MC.world.getBlockState(MC.player.getBlockPos().up()).getBlock() != Block.getBlockFromItem(Items.STRING)) {
            int prev = MC.player.getInventory().selectedSlot;
            if(MC.player.getInventory().getMainHandStack().getItem() != Items.STRING) {
                int slot = InventoryUtils.findItemInHotbar(Items.STRING);
                if(slot != -1) {
                    MC.player.getInventory().selectedSlot = slot;
                    MC.player.networkHandler.sendPacket(new UpdateSelectedSlotC2SPacket(MC.player.getInventory().selectedSlot));
                }
            }
            WorldUtils.placeBlockMainHand(rotate.getValue(), key, key, true, false, MC.player.getBlockPos().up());
            MC.player.getInventory().selectedSlot = prev;
        }
    }
}
