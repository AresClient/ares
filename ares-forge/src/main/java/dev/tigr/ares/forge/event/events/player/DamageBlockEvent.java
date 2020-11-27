package dev.tigr.ares.forge.event.events.player;

import dev.tigr.simpleevents.event.Event;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;

/**
 * @author Hoosiers on 11/26/2020
 */
public class DamageBlockEvent extends Event {
    private final BlockPos blockPos;
    private final EnumFacing enumFacing;

    public DamageBlockEvent(BlockPos blockPos, EnumFacing enumFacing) {
        this.blockPos = blockPos;
        this.enumFacing = enumFacing;
    }

    public BlockPos getBlockPos() {
        return this.blockPos;
    }

    public EnumFacing getEnumFacing() {
        return this.enumFacing;
    }
}