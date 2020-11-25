package dev.tigr.ares.forge.impl.modules.combat;

import dev.tigr.ares.core.feature.module.Category;
import dev.tigr.ares.core.feature.module.Module;
import dev.tigr.ares.core.setting.Setting;
import dev.tigr.ares.core.setting.settings.numerical.IntegerSetting;
import net.minecraft.item.ItemBow;
import net.minecraft.network.play.client.CPacketPlayerDigging;
import net.minecraft.network.play.client.CPacketPlayerTryUseItem;
import net.minecraft.util.math.BlockPos;

/**
 * @author Tigermouthbear
 */
@Module.Info(name = "BowRelease", description = "Automatically release bow at a delay", category = Category.COMBAT)
public class BowRelease extends Module {
    private final Setting<Integer> delay = register(new IntegerSetting("Delay", 20, 3, 20));

    @Override
    public void onTick() {
        if(MC.player.inventory.getCurrentItem().getItem() instanceof ItemBow && MC.player.isHandActive() && MC.player.getItemInUseCount() >= delay.getValue()) {
            MC.player.connection.sendPacket(new CPacketPlayerDigging(CPacketPlayerDigging.Action.RELEASE_USE_ITEM, BlockPos.ORIGIN, MC.player.getHorizontalFacing()));
            MC.player.connection.sendPacket(new CPacketPlayerTryUseItem(MC.player.getActiveHand()));
            MC.player.stopActiveHand();
        }
    }
}
