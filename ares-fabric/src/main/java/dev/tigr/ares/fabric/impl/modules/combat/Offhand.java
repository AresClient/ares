package dev.tigr.ares.fabric.impl.modules.combat;

import dev.tigr.ares.core.feature.module.Category;
import dev.tigr.ares.core.feature.module.Module;
import dev.tigr.ares.core.setting.Setting;
import dev.tigr.ares.core.setting.settings.BooleanSetting;
import dev.tigr.ares.core.setting.settings.EnumSetting;
import dev.tigr.ares.core.setting.settings.numerical.FloatSetting;
import dev.tigr.ares.fabric.utils.InventoryUtils;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.screen.slot.SlotActionType;

/**
 * @author Tigermouthbear 9/16/20
 * ported to 1.16 on 12/5/20
 */
@Module.Info(name = "Offhand", description = "Automatically puts an item in your offhand", category = Category.COMBAT, alwaysListening = true)
public class Offhand extends Module {
    enum SItem { CRYSTAL, GAPPLE, BOW }

    private final Setting<SItem> item = register(new EnumSetting<>("Item", SItem.CRYSTAL));
    private final Setting<Float> minHealth = register(new FloatSetting("Minimum Health", 10.0f, 0.0f, 15.0f));
    private final Setting<Boolean> stayOn = register(new BooleanSetting("Stay On", true));

    private boolean clickBlank = false;
    private boolean move = false;

    @Override
    public void onEnable() {
        AutoTotem.INSTANCE.soft.setValue(true);
        checkOffhand();
    }

    @Override
    public void onTick() {
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

        if(getEnabled()) checkOffhand();
    }

    @Override
    public void onDisable() {
        disable();
    }

    private void disable() {
        if(MC.player.getOffHandStack().getItem() == getCurrItem()) {
            int index = InventoryUtils.findItem(Items.TOTEM_OF_UNDYING);
            if(index == -1) index = InventoryUtils.getBlank();
            if(index == -1) return;
            MC.interactionManager.clickSlot(0, InventoryUtils.getSlotIndex(index), 0, SlotActionType.PICKUP, MC.player);
            move = true;
        }
    }

    private void checkOffhand() {
        if(MC.player.getHealth() < minHealth.getValue()) {
            if(!stayOn.getValue()) setEnabled(false);
            else disable();
            return;
        }

        if(MC.player.getOffHandStack().getItem() != getCurrItem()) {
            int index = InventoryUtils.findItem(getCurrItem());
            if(index == -1) {
                setEnabled(false);
                return;
            }
            MC.interactionManager.clickSlot(0, InventoryUtils.getSlotIndex(index), 0, SlotActionType.PICKUP, MC.player);
            move = true;
        }
    }

    private Item getCurrItem() {
        return item.getValue() == SItem.CRYSTAL ? Items.END_CRYSTAL : (item.getValue() == SItem.GAPPLE ? Items.GOLDEN_APPLE : Items.BOW);
    }
}
