package dev.tigr.ares.fabric.impl.modules.combat;

import dev.tigr.ares.core.feature.module.Category;
import dev.tigr.ares.core.feature.module.Module;
import dev.tigr.ares.core.setting.Setting;
import dev.tigr.ares.core.setting.settings.BooleanSetting;
import dev.tigr.ares.fabric.event.client.SetKeyBindingStateEvent;
import dev.tigr.ares.fabric.utils.InventoryUtils;
import dev.tigr.simpleevents.listener.EventHandler;
import dev.tigr.simpleevents.listener.EventListener;
import net.minecraft.item.Items;
import net.minecraft.item.SwordItem;
import net.minecraft.item.ToolItem;
import net.minecraft.screen.slot.SlotActionType;

/**
 * @author Tigermouthbear 7/14/20
 * updated to 1.16 on 12/11/20
 */
@Module.Info(name = "OffhandGap", description = "Eats a gapple in your offhand when you right click while holding a tool", category = Category.COMBAT)
public class OffhandGap extends Module {
    public static OffhandGap INSTANCE;
    
    private final Setting<Boolean> autoCrystal = register(new BooleanSetting("While AutoCrystal", true));

    private boolean clickBlank = false;
    private boolean move = false;
    private boolean gapping = false;
    private int targetIndex = -1;

    public OffhandGap() {
        INSTANCE = this;
    }

    @EventHandler
    public EventListener<SetKeyBindingStateEvent> mouseClickEvent = new EventListener<>(event -> {
        if(event.getKeyBinding().getTranslationKey().equals(MC.options.keyUse.getBoundKeyTranslationKey())) {
            if(event.getState() && shouldOffhand()) {
                targetIndex = InventoryUtils.findItem(Items.ENCHANTED_GOLDEN_APPLE);
                if(targetIndex == -1) targetIndex = InventoryUtils.findItem(Items.GOLDEN_APPLE);
                gapping = true;
                MC.interactionManager.clickSlot(0, InventoryUtils.getSlotIndex(targetIndex), 0, SlotActionType.PICKUP, MC.player);
                move = true;
            } else if(!event.getState()) {
                if(!gapping) return;
                gapping = false;
                int index = InventoryUtils.findItem(Items.TOTEM_OF_UNDYING);
                if(index == -1) index = InventoryUtils.getBlank();
                if(index == -1) return;
                MC.interactionManager.clickSlot(0, InventoryUtils.getSlotIndex(index), 0, SlotActionType.PICKUP, MC.player);
                move = true;
            }
        }
    });

    @Override
    public void onTick() {
        if(move) {
            MC.interactionManager.clickSlot(0, 45, 0, SlotActionType.PICKUP, MC.player);
            move = false;
            if(!MC.player.currentScreenHandler.getCursorStack().isEmpty()) clickBlank = true;
            return;
        }

        if(clickBlank) {
            int index = targetIndex;
            if(index == -1) index = InventoryUtils.getBlank();
            if(index == -1) return;
            MC.interactionManager.clickSlot(0, InventoryUtils.getSlotIndex(index), 0, SlotActionType.PICKUP, MC.player);
            clickBlank = false;
        }
    }

    private boolean shouldOffhand() {
        return (InventoryUtils.findItem(Items.GOLDEN_APPLE) != -1 || InventoryUtils.findItem(Items.ENCHANTED_GOLDEN_APPLE) != -1)
                && (MC.player.getMainHandStack().getItem() instanceof ToolItem || MC.player.getMainHandStack().getItem() instanceof SwordItem
                || (autoCrystal.getValue() && CrystalAura.INSTANCE.getEnabled() && MC.player.getMainHandStack().getItem() == Items.END_CRYSTAL));
    }
}
