package dev.tigr.ares.core.util.tracker;

import dev.tigr.ares.core.event.client.PacketEvent;
import dev.tigr.ares.core.util.global.Tracker;
import dev.tigr.simpleevents.listener.EventHandler;
import dev.tigr.simpleevents.listener.EventListener;

/**
 * @author Makrennel
 * This is for management of the hotbar slot / slot packets
 */

public class HotbarTracker extends Tracker {
    int modifySlot = -1;
    int currentSlot = -1;
    int oldSlot = -1;

    public void setSlot(int modifySlot, boolean packetPlace, int oldSlot) {
        this.modifySlot = modifySlot;

        // Cannot interactionManager / playerController place without changing the actual hotbar slot
        if(!packetPlace) {
            this.oldSlot = oldSlot;
            INV.setCurrentSlot(modifySlot);
        }
    }

    public void sendSlot() {
        if(modifySlot != -1) PACKET.hotbarSlotUpdate(modifySlot);
    }

    public int getCurrentSlot() {
        return currentSlot;
    }

    public void reset() {
        // Make sure to reset the visual hotbar first if it has been modified
        if(oldSlot != -1) {
            INV.setCurrentSlot(oldSlot);
            oldSlot = -1;
        }

        // Send a packet to sync the slot
        if(currentSlot != INV.getCurrentSlot())
            PACKET.hotbarSlotUpdate(INV.getCurrentSlot());

        modifySlot = -1;
    }

    @EventHandler
    private final EventListener<PacketEvent.Sent.HotbarSlot> hotbarSlotPacket = new EventListener<>(event -> {
        if(modifySlot != -1 && event.getSlot() != modifySlot) {
            // Ensure no packets are sent with a slot that doesn't match the intended slot while this is active
            event.setSlot(modifySlot);
            currentSlot = modifySlot;
        }
        // So that we can keep track of the current slot the server thinks we are on in case it doesn't match the visual hotbar
        else currentSlot = event.getSlot();

        INV.setLastSelectedSlot(currentSlot);
    });
}
