package dev.tigr.ares.forge.event.events.movement;

import dev.tigr.simpleevents.event.Event;

/**
 * @author Makrennel
 */
public class BlockPushEvent extends Event {
    public double var1;
    public double var2;
    public double var3;

    public BlockPushEvent(double var1, double var2, double var3) {
        this.setCancelled(false);
        this.var1 = var1;
        this.var2 = var2;
        this.var3 = var3;
    }
}
