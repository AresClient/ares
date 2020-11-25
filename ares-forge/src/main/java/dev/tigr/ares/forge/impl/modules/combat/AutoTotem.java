package dev.tigr.ares.forge.impl.modules.combat;

import dev.tigr.ares.core.feature.module.Category;
import dev.tigr.ares.core.feature.module.Module;
import dev.tigr.ares.core.setting.Setting;
import dev.tigr.ares.core.setting.settings.BooleanSetting;
import dev.tigr.ares.forge.utils.InventoryUtils;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.init.Items;
import net.minecraft.inventory.ClickType;

/**
 * @author Tigermouthbear
 */
@Module.Info(name = "AutoTotem", description = "Automatically equips a totem when you pop one", category = Category.COMBAT)
public class AutoTotem extends Module {
    public final Setting<Boolean> soft = register(new BooleanSetting("Soft", false));

    public static AutoTotem INSTANCE;

    private int totemCount = 0;
    private boolean clickBlank = false;
    private boolean move = false;

    public AutoTotem() {
        INSTANCE = this;
    }

    @Override
    public void onTick() {
        if(MC.currentScreen instanceof GuiContainer) return;
        totemCount = InventoryUtils.amountInInventory(Items.TOTEM_OF_UNDYING);

        if(move) {
            MC.playerController.windowClick(0, 45, 0, ClickType.PICKUP, MC.player);
            move = false;
            if(!MC.player.inventory.getItemStack().isEmpty()) clickBlank = true;
            return;
        }

        if(clickBlank) {
            int index = InventoryUtils.getBlank();
            if(index == -1) return;
            MC.playerController.windowClick(0, InventoryUtils.getSlotIndex(index), 0, ClickType.PICKUP, MC.player);
            clickBlank = false;
        }

        if(MC.player.getHeldItemOffhand().getItem() != Items.TOTEM_OF_UNDYING) {
            if((soft.getValue() || (OffhandGap.INSTANCE.getEnabled() && MC.player.getHeldItemOffhand().getItem() == Items.GOLDEN_APPLE)) && !MC.player.getHeldItemOffhand().isEmpty())
                return;

            if(totemCount == 0) return;
            int index = InventoryUtils.findItem(Items.TOTEM_OF_UNDYING);
            if(index == -1) return;
            MC.playerController.windowClick(0, InventoryUtils.getSlotIndex(index), 0, ClickType.PICKUP, MC.player);
            move = true;
        }
    }

    @Override
    public String getInfo() {
        return String.valueOf(totemCount);
    }
}
