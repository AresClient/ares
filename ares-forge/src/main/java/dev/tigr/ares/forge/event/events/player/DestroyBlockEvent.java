package dev.tigr.ares.forge.event.events.player;

import net.minecraft.util.math.BlockPos;

public class DestroyBlockEvent {
    private final BlockPos pos;

    public DestroyBlockEvent(BlockPos pos) {
        this.pos = pos;
    }

    public BlockPos getPos() {
        return pos;
    }
}
