package org.aresclient.ares.api.event.client;

import org.aresclient.ares.api.event.AresEvent;

// TODO: FIGURE OUT A WAY TO PASS SCREEN THROUGH EVENT
// rn ares needs to know if its the main menu, so this is all this does
public class ScreenOpenedEvent extends AresEvent {
    private final boolean mainMenu;

    public ScreenOpenedEvent(boolean mainMenu) {
        super("screen_open", null);
        this.mainMenu = mainMenu;
    }

    public boolean isMainMenu() {
        return mainMenu;
    }
}
