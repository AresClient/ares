package dev.tigr.ares.fabric.event.client;

import dev.tigr.simpleevents.event.Event;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.screen.slot.SlotActionType;

public class ClickSlotEvent extends Event {
    int slotId, button;
    SlotActionType actionType;
    PlayerEntity player;

    public ClickSlotEvent(int slotId, int button, SlotActionType actionType, PlayerEntity player) {
        this.slotId = slotId;
        this.button = button;
        this.actionType = actionType;
        this.player = player;
    }

    public int getSlotId() {
        return slotId;
    }

    public int getButton() {
        return button;
    }

    public SlotActionType getActionType() {
        return actionType;
    }

    public PlayerEntity getPlayer() {
        return player;
    }
}
