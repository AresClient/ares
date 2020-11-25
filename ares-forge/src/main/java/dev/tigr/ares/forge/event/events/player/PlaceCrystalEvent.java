package dev.tigr.ares.forge.event.events.player;

import dev.tigr.simpleevents.event.Event;
import net.minecraft.util.math.BlockPos;

public class PlaceCrystalEvent extends Event {
    private final BlockPos pos;

    public PlaceCrystalEvent(BlockPos pos) {
        this.pos = pos;
    }

    public BlockPos getPos() {
        return pos;
    }
}
