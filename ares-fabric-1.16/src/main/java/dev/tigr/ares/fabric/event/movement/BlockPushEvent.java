package dev.tigr.ares.fabric.event.movement;

import dev.tigr.simpleevents.event.Event;

/**
 * @author Makrennel
 */
public class BlockPushEvent extends Event {
    public double var1;
    public double var2;

    public BlockPushEvent(double var1, double var2) {
        this.setCancelled(false);
        this.var1 = var1;
        this.var2 = var2;
    }
}
