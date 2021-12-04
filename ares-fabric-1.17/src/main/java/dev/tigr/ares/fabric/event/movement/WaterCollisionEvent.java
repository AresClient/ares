package dev.tigr.ares.fabric.event.movement;

import dev.tigr.simpleevents.event.Event;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;

public class WaterCollisionEvent extends Event {
    private final BlockPos pos;
    private VoxelShape shape = null;

    public WaterCollisionEvent(BlockPos pos) {
        this.pos = pos;
    }

    public BlockPos getPos() {
        return pos;
    }

    public VoxelShape getShape() {
        return shape;
    }

    public void setShape(VoxelShape shape) {
        this.shape = shape;
    }
}

