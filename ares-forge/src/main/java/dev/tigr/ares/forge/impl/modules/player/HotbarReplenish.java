package dev.tigr.ares.forge.impl.modules.player;

import dev.tigr.ares.core.feature.module.Category;
import dev.tigr.ares.core.feature.module.Module;
import dev.tigr.ares.core.setting.Setting;
import dev.tigr.ares.core.setting.settings.numerical.IntegerSetting;
import dev.tigr.ares.forge.utils.InventoryUtils;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.inventory.ClickType;
import net.minecraft.item.Item;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Tigermouthbear
 */
@Module.Info(name = "HotbarReplenish", description = "Automatically replenishes the itemstacks on your hotbar", category = Category.PLAYER)
public class HotbarReplenish extends Module {
    private final Setting<Integer> pollRate = register(new IntegerSetting("Poll Rate", 2,0, 20));

    private final Map<Integer, Item> hotbar = new HashMap<>();

    private boolean clickBlank = false;
    private int move = -1;

    private boolean enable = true;

    @Override
    public void onDisable() {
        enable = true;
    }

    @Override
    public void onTick() {
        if((MC.currentScreen instanceof GuiContainer && MC.player != null) || enable) {
            for(int i = 0; i < 9; i++) hotbar.put(i, MC.player.inventory.getStackInSlot(i).getItem());
            enable = false;
        }

        if(MC.currentScreen instanceof GuiContainer || MC.player.ticksExisted % pollRate.getValue() != 0) return;

        if(clickBlank) {
            int index = InventoryUtils.getBlank();
            if(index == -1) return;
            MC.playerController.windowClick(0, InventoryUtils.getSlotIndex(index), 0, ClickType.PICKUP, MC.player);
            clickBlank = false;
        }

        if(move != -1) {
            MC.playerController.windowClick(0, InventoryUtils.getSlotIndex(move), 0, ClickType.PICKUP, MC.player);
            move = -1;
            if(!MC.player.inventory.getItemStack().isEmpty()) clickBlank = true;
            return;
        }

        for(int stack: hotbar.keySet()) {
            Item item = hotbar.get(stack);
            if(MC.player.inventory.getStackInSlot(stack).getItem() != item) {
                if(MC.player.inventory.getStackInSlot(stack).isEmpty()) {
                    int count = InventoryUtils.amountInInventory(item);
                    if(count == 0) continue;
                    int index = -1;
                    for(int i = 9; i < 45; i++) {
                        if(MC.player.inventory.getStackInSlot(i).getItem() == item) {
                            index = i;
                            break;
                        }
                    }
                    if(index == -1) continue;
                    MC.playerController.windowClick(0, InventoryUtils.getSlotIndex(index), 0, ClickType.PICKUP, MC.player);
                    move = stack;
                    return;
                } else {
                    hotbar.put(stack, MC.player.inventory.getStackInSlot(stack).getItem());
                }
            }
        }
    }
}
