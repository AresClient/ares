package dev.tigr.ares.forge.event.events.movement;

import dev.tigr.simpleevents.event.Event;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;

public class WaterCollisionBoxEvent extends Event {
    private final BlockPos pos;
    private AxisAlignedBB bb = null;

    public WaterCollisionBoxEvent(BlockPos pos) {
        this.pos = pos;
    }

    public BlockPos getPos() {
        return pos;
    }

    public AxisAlignedBB getBoundingBox() {
        return bb;
    }

    public void setBoundingBox(AxisAlignedBB bb) {
        this.bb = bb;
    }
}
