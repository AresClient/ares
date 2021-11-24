package dev.tigr.ares.forge.utils;

import dev.tigr.ares.Wrapper;
import dev.tigr.ares.core.Ares;
import dev.tigr.ares.forge.event.events.player.PacketEvent;
import dev.tigr.ares.forge.mixin.accessor.PlayerControllerMPAccessor;
import dev.tigr.simpleevents.listener.EventHandler;
import dev.tigr.simpleevents.listener.EventListener;
import net.minecraft.network.play.client.CPacketHeldItemChange;

/**
 * @author Makrennel
 * This is for management of the hotbar slot / slot packets
 */

public class HotbarTracker implements Wrapper {
    public static final HotbarTracker HOTBAR_TRACKER = new HotbarTracker();

    public HotbarTracker() {
    }

    int connections = -1;

    public void connect() {
        connections++;
        if(connections == 0) Ares.EVENT_MANAGER.register(this);
    }

    public void disconnect() {
        connections--;
        if(connections == -1) Ares.EVENT_MANAGER.unregister(this);
    }

    int modifySlot = -1;
    int currentSlot = -1;
    int oldSlot = -1;

    public void setSlot(int modifySlot, boolean packetPlace, int oldSlot) {
        this.modifySlot = modifySlot;

        // Cannot interactionManager / playerController place without changing the actual hotbar slot
        if(!packetPlace) {
            this.oldSlot = oldSlot;
            MC.player.inventory.currentItem = modifySlot;
        }
    }

    public void sendSlot() {
        if(modifySlot != -1) MC.player.connection.sendPacket(new CPacketHeldItemChange(modifySlot));
    }

    public int getCurrentSlot() {
        return currentSlot;
    }

    public void reset() {
        // Make sure to reset the visual hotbar first if it has been modified
        if(oldSlot != -1) {
            MC.player.inventory.currentItem = oldSlot;
            oldSlot = -1;
        }

        // Send a packet to sync the slot
        if(currentSlot != MC.player.inventory.currentItem)
            MC.player.connection.sendPacket(new CPacketHeldItemChange(MC.player.inventory.currentItem));

        modifySlot = -1;
    }

    @EventHandler
    private EventListener<PacketEvent.Sent> packetSentEvent = new EventListener<>(event -> {
        if(event.getPacket() instanceof CPacketHeldItemChange) {
            CPacketHeldItemChange packet = (CPacketHeldItemChange) event.getPacket();
            if(modifySlot != -1 && packet.getSlotId() != modifySlot) {
                // Ensure no packets are sent with a slot that doesn't match the intended slot while this is active
                event.setCancelled(true);
                MC.player.connection.sendPacket(new CPacketHeldItemChange(modifySlot));
                currentSlot = modifySlot;
            }
            // So that we can keep track of the current slot the server thinks we are on in case it doesn't match the visual hotbar
            else currentSlot = packet.getSlotId();

            ((PlayerControllerMPAccessor) MC.playerController).setLastSelectedSlot(currentSlot);
        }
    });
}
