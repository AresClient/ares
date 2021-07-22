package dev.tigr.ares.fabric.impl.modules.combat;

import dev.tigr.ares.core.feature.module.Category;
import dev.tigr.ares.core.feature.module.Module;
import dev.tigr.ares.core.setting.Setting;
import dev.tigr.ares.core.setting.settings.numerical.IntegerSetting;
import net.minecraft.item.BowItem;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractItemC2SPacket;
import net.minecraft.util.math.BlockPos;

/**
 * @author Tigermouthbear
 */
@Module.Info(name = "BowRelease", description = "Automatically release bow at a delay", category = Category.COMBAT)
public class BowRelease extends Module {
    private final Setting<Integer> delay = register(new IntegerSetting("Delay", 20, 3, 20));

    @Override
    public void onTick() {
        if(MC.player.getMainHandStack().getItem() instanceof BowItem && MC.player.isUsingItem() && MC.player.getItemUseTime() >= delay.getValue()) {
            MC.player.networkHandler.sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.RELEASE_USE_ITEM, BlockPos.ORIGIN, MC.player.getHorizontalFacing()));
            MC.player.networkHandler.sendPacket(new PlayerInteractItemC2SPacket(MC.player.getActiveHand()));
            MC.player.stopUsingItem();
        }
    }
}
