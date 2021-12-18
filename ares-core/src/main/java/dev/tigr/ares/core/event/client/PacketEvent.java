package dev.tigr.ares.core.event.client;

import dev.tigr.simpleevents.event.Event;

public class PacketEvent {
    public static class Sent {
        public static class HotbarSlot extends Event {
            int slot;

            public HotbarSlot(int slot) {
                this.slot = slot;
            }

            public int getSlot() {
                return slot;
            }

            public void setSlot(int slot) {
                this.slot = slot;
            }
        }

        public static class Input extends Event {
            public float sideways, forward;
            public boolean jumping, sneaking;

            public Input(float sideways, float forward, boolean jumping, boolean sneaking) {
                this.sideways = sideways;
                this.forward = forward;
                this.jumping = jumping;
                this.sneaking = sneaking;
            }
        }
    }
}
