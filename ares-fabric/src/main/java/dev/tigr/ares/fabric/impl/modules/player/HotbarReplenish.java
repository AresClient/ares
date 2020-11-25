package dev.tigr.ares.fabric.impl.modules.player;

import dev.tigr.ares.core.feature.module.Category;
import dev.tigr.ares.core.feature.module.Module;
import dev.tigr.ares.fabric.utils.InventoryUtils;
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen;
import net.minecraft.item.Item;
import net.minecraft.screen.slot.SlotActionType;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Tigermouthbear
 * updated to 1.16.2 on 9/5/20
 */
@Module.Info(name = "HotbarReplenish", description = "Automatically replenishes the itemstacks on your hotbar", category = Category.PLAYER)
public class HotbarReplenish extends Module {
    private final Map<Integer, Item> hotbar = new HashMap<>();

    private boolean clickBlank = false;
    private int move = -1;

    @Override
    public void onEnable() {
        for(int i = 0; i < 9; i++) hotbar.put(i, MC.player.inventory.getStack(i).getItem());
    }

    @Override
    public void onTick() {
        if(MC.currentScreen instanceof GenericContainerScreen && MC.player != null) {
            for(int i = 0; i < 9; i++) hotbar.put(i, MC.player.inventory.getStack(i).getItem());
        }

        if(MC.currentScreen instanceof GenericContainerScreen || TICKS % 2 != 0) return;

        if(clickBlank) {
            int index = InventoryUtils.getBlank();
            if(index == -1) return;
            MC.interactionManager.clickSlot(0, InventoryUtils.getSlotIndex(index), 0, SlotActionType.PICKUP, MC.player);
            clickBlank = false;
        }

        if(move != -1) {
            MC.interactionManager.clickSlot(0, InventoryUtils.getSlotIndex(move), 0, SlotActionType.PICKUP, MC.player);
            move = -1;
            if(!MC.player.inventory.getCursorStack().isEmpty()) clickBlank = true;
            return;
        }

        for(int stack: hotbar.keySet()) {
            if(MC.player.inventory.getStack(stack).getItem() != hotbar.get(stack)) {
                if(MC.player.inventory.getStack(stack).isEmpty()) {
                    int count = InventoryUtils.amountInInventory(hotbar.get(stack));
                    if(count == 0) continue;
                    int index = -1;
                    for(int i = 9; i < 45; i++) {
                        if(MC.player.inventory.getStack(i).getItem() == hotbar.get(stack)) {
                            index = i;
                            break;
                        }
                    }
                    if(index == -1) continue;
                    MC.interactionManager.clickSlot(0, InventoryUtils.getSlotIndex(index), 0, SlotActionType.PICKUP, MC.player);
                    move = stack;
                    return;
                } else {
                    hotbar.put(stack, MC.player.inventory.getStack(stack).getItem());
                }
            }
        }
    }
}
