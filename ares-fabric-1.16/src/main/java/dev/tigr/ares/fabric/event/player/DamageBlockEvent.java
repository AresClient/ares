package dev.tigr.ares.fabric.event.player;

import dev.tigr.simpleevents.event.Event;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

/**
 * @author Hoosiers on 11/26/20
 */
public class DamageBlockEvent extends Event {
    private final BlockPos blockPos;
    private final Direction direction;

    public DamageBlockEvent(BlockPos blockPos, Direction direction) {
        this.blockPos = blockPos;
        this.direction = direction;
    }

    public BlockPos getBlockPos() {
        return blockPos;
    }

    public Direction getDirection() {
        return direction;
    }
}