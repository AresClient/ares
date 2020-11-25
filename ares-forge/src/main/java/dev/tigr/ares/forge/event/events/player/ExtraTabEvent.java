package dev.tigr.ares.forge.event.events.player;

import dev.tigr.simpleevents.event.Event;

public class ExtraTabEvent extends Event {
    private int num = 80;

    public int getNum() {
        return num;
    }

    public void setNum(int num) {
        this.num = num;
    }
}
