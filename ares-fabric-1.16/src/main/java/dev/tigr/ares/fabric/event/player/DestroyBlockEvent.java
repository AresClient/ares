package dev.tigr.ares.fabric.event.player;

import dev.tigr.simpleevents.event.Event;
import net.minecraft.util.math.BlockPos;

public class DestroyBlockEvent extends Event {
    private final BlockPos pos;

    public DestroyBlockEvent(BlockPos pos) {
        this.pos = pos;
    }

    public BlockPos getPos() {
        return pos;
    }
}
