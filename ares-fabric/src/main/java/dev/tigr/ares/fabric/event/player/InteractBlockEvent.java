package dev.tigr.ares.fabric.event.player;

import dev.tigr.simpleevents.event.Event;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;

public class InteractBlockEvent extends Event {
    public ClientPlayerEntity player;
    public ClientWorld world;
    public Hand hand;
    public BlockHitResult blockHitResult;

    ActionResult returnValue;

    public InteractBlockEvent(ClientPlayerEntity player, ClientWorld world, Hand hand, BlockHitResult blockHitResult) {
        this.player = player;
        this.world = world;
        this.hand = hand;
        this.blockHitResult = blockHitResult;
    }

    public void setReturnValue(ActionResult returnValue) {
        this.returnValue = returnValue;
    }

    public ActionResult getReturnValue() {
        return returnValue;
    }
}
