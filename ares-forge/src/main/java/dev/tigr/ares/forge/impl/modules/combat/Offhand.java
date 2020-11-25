package dev.tigr.ares.forge.impl.modules.combat;

import dev.tigr.ares.core.feature.module.Category;
import dev.tigr.ares.core.feature.module.Module;
import dev.tigr.ares.core.setting.Setting;
import dev.tigr.ares.core.setting.settings.BooleanSetting;
import dev.tigr.ares.core.setting.settings.EnumSetting;
import dev.tigr.ares.core.setting.settings.numerical.FloatSetting;
import dev.tigr.ares.forge.utils.InventoryUtils;
import net.minecraft.init.Items;
import net.minecraft.inventory.ClickType;
import net.minecraft.item.Item;

/**
 * @author Tigermouthbear 9/16/20
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

        if(getEnabled()) checkOffhand();
    }

    @Override
    public void onDisable() {
        disable();
    }

    private void disable() {
        if(MC.player.getHeldItemOffhand().getItem() == getCurrItem()) {
            int index = InventoryUtils.findItem(Items.TOTEM_OF_UNDYING);
            if(index == -1) index = InventoryUtils.getBlank();
            if(index == -1) return;
            MC.playerController.windowClick(0, InventoryUtils.getSlotIndex(index), 0, ClickType.PICKUP, MC.player);
            move = true;
        }
    }

    private void checkOffhand() {
        if(MC.player.getHealth() < minHealth.getValue()) {
            if(!stayOn.getValue()) setEnabled(false);
            else disable();
            return;
        }

        if(MC.player.getHeldItemOffhand().getItem() != getCurrItem()) {
            int index = InventoryUtils.findItem(getCurrItem());
            if(index == -1) {
                setEnabled(false);
                return;
            }
            MC.playerController.windowClick(0, InventoryUtils.getSlotIndex(index), 0, ClickType.PICKUP, MC.player);
            move = true;
        }
    }

    private Item getCurrItem() {
        return item.getValue() == SItem.CRYSTAL ? Items.END_CRYSTAL : (item.getValue() == SItem.GAPPLE ? Items.GOLDEN_APPLE : Items.BOW);
    }
}
