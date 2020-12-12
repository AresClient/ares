package dev.tigr.ares.forge.impl.modules.combat;

import dev.tigr.ares.core.feature.module.Category;
import dev.tigr.ares.core.feature.module.Module;
import dev.tigr.ares.core.setting.Setting;
import dev.tigr.ares.core.setting.settings.BooleanSetting;
import dev.tigr.ares.forge.utils.InventoryUtils;
import dev.tigr.simpleevents.listener.EventHandler;
import dev.tigr.simpleevents.listener.EventListener;
import net.minecraft.init.Items;
import net.minecraft.inventory.ClickType;
import net.minecraft.item.ItemSword;
import net.minecraft.item.ItemTool;
import net.minecraftforge.client.event.MouseEvent;

/**
 * @author Tigermouthbear 7/14/20
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
    public EventListener<MouseEvent> mouseClickEvent = new EventListener<>(event -> {
        if(event.getButton() == 1) {
            if(event.isButtonstate() && shouldOffhand()) {
                targetIndex = InventoryUtils.findItem(Items.GOLDEN_APPLE);
                gapping = true;
                MC.playerController.windowClick(0, InventoryUtils.getSlotIndex(targetIndex), 0, ClickType.PICKUP, MC.player);
                move = true;
            } else if(!event.isButtonstate()) {
                if(!gapping) return;
                gapping = false;
                int index = InventoryUtils.findItem(Items.TOTEM_OF_UNDYING);
                if(index == -1) index = InventoryUtils.getBlank();
                if(index == -1) return;
                MC.playerController.windowClick(0, InventoryUtils.getSlotIndex(index), 0, ClickType.PICKUP, MC.player);
                move = true;
            }
        }
    });

    @Override
    public void onTick() {
        if(move) {
            MC.playerController.windowClick(0, 45, 0, ClickType.PICKUP, MC.player);
            move = false;
            if(!MC.player.inventory.getItemStack().isEmpty()) clickBlank = true;
            return;
        }

        if(clickBlank) {
            int index = targetIndex;
            if(index == -1) index = InventoryUtils.getBlank();
            if(index == -1) return;
            MC.playerController.windowClick(0, InventoryUtils.getSlotIndex(index), 0, ClickType.PICKUP, MC.player);
            clickBlank = false;
        }
    }

    private boolean shouldOffhand() {
        return InventoryUtils.findItem(Items.GOLDEN_APPLE) != -1
                && (MC.player.getHeldItemMainhand().getItem() instanceof ItemTool || MC.player.getHeldItemMainhand().getItem() instanceof ItemSword
                || (autoCrystal.getValue() && CrystalAura.INSTANCE.getEnabled() && MC.player.getHeldItemMainhand().getItem() == Items.END_CRYSTAL));
    }
}
