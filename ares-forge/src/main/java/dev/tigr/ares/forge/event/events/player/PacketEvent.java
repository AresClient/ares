package dev.tigr.ares.forge.event.events.player;

import dev.tigr.simpleevents.event.Event;
import net.minecraft.network.Packet;

public class PacketEvent extends Event {
    private Packet<?> packet;

    public PacketEvent(Packet<?> packet) {
        this.packet = packet;
    }

    public Packet<?> getPacket() {
        return packet;
    }

    public void setPacket(Packet<?> packet) {
        this.packet = packet;
    }

    public static class Receive extends PacketEvent {
        public Receive(Packet<?> packet) {
            super(packet);
        }
    }

    public static class Sent extends PacketEvent {
        public Sent(Packet<?> packet) {
            super(packet);
        }
    }
}
