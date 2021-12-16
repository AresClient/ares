package dev.tigr.ares.core.event.client;

import dev.tigr.simpleevents.event.Event;

public class PacketEvent {
    public static class Sent {
        public static class HotbarSlotPacket extends Event {
            int slot;

            public HotbarSlotPacket(int slot) {
                this.slot = slot;
            }

            public int getSlot() {
                return slot;
            }

            public void setSlot(int slot) {
                this.slot = slot;
            }
        }
    }
}
