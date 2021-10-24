package dev.tigr.ares.forge.event.events.player;

import dev.tigr.simpleevents.event.Event;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public class InteractBlockEvent extends Event {
    public EntityPlayerSP player;
    public WorldClient world;
    public EnumHand hand;
    public BlockPos pos;
    public EnumFacing direction;
    public Vec3d vec;

    EnumActionResult returnValue;

    public InteractBlockEvent(EntityPlayerSP player, WorldClient world, EnumHand hand, BlockPos pos, EnumFacing direction, Vec3d vec) {
        this.player = player;
        this.world = world;
        this.hand = hand;
        this.pos = pos;
        this.direction = direction;
        this.vec = vec;
    }

    public void setReturnValue(EnumActionResult returnValue) {
        this.returnValue = returnValue;
    }

    public EnumActionResult getReturnValue() {
        return returnValue;
    }
}
