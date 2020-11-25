package dev.tigr.ares.fabric.impl.modules.combat;

import dev.tigr.ares.core.feature.module.Category;
import dev.tigr.ares.core.feature.module.Module;
import dev.tigr.ares.core.setting.Setting;
import dev.tigr.ares.core.setting.settings.BooleanSetting;
import dev.tigr.ares.fabric.utils.InventoryUtils;
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen;
import net.minecraft.item.Items;
import net.minecraft.screen.slot.SlotActionType;

/**
 * @author Tigermouthbear
 */
@Module.Info(name = "AutoTotem", description = "Automatically equips a totem when you pop one", category = Category.COMBAT)
public class AutoTotem extends Module {
    private final Setting<Boolean> soft = register(new BooleanSetting("Soft", false));

    public static AutoTotem INSTANCE;

    private int totemCount = 0;
    private boolean clickBlank = false;
    private boolean move = false;

    public AutoTotem() {
        INSTANCE = this;
    }

    @Override
    public void onTick() {
        if(MC.currentScreen instanceof GenericContainerScreen) return;
        totemCount = InventoryUtils.amountInInventory(Items.TOTEM_OF_UNDYING);

        if(move) {
            MC.interactionManager.clickSlot(0, 45, 0, SlotActionType.PICKUP, MC.player);
            move = false;
            if(!MC.player.inventory.getCursorStack().isEmpty()) clickBlank = true;
            return;
        }

        if(clickBlank) {
            int index = InventoryUtils.getBlank();
            if(index == -1) return;
            MC.interactionManager.clickSlot(0, InventoryUtils.getSlotIndex(index), 0, SlotActionType.PICKUP, MC.player);
            clickBlank = false;
        }

        if(MC.player.getOffHandStack().getItem() != Items.TOTEM_OF_UNDYING) {
            if(/*(*/soft.getValue() /*|| (OffhandGap.INSTANCE.getEnabled() && MC.player.getOffHandStack().getItem() == Items.GOLDEN_APPLE))*/ && !MC.player.getOffHandStack().isEmpty())
                return;

            if(totemCount == 0) return;
            int index = InventoryUtils.findItem(Items.TOTEM_OF_UNDYING);
            if(index == -1) return;
            MC.interactionManager.clickSlot(0, InventoryUtils.getSlotIndex(index), 0, SlotActionType.PICKUP, MC.player);
            move = true;
        }
    }

    @Override
    public String getInfo() {
        return String.valueOf(totemCount);
    }
}
