package dev.tigr.ares.forge.event.events.player;

import dev.tigr.simpleevents.event.Event;
import net.minecraft.network.play.server.SPacketChunkData;
import net.minecraft.world.chunk.Chunk;

public class ChunkLoadEvent extends Event {
    private final Chunk chunk;
    private final SPacketChunkData data;

    public ChunkLoadEvent(Chunk chunk, SPacketChunkData data) {
        this.chunk = chunk;
        this.data = data;
    }

    public Chunk getChunk() {
        return chunk;
    }

    public SPacketChunkData getData() {
        return data;
    }
}
