package dev.tigr.ares.core.gui.impl.menu;

import dev.tigr.ares.core.feature.module.ClickGUIMod;
import dev.tigr.ares.core.gui.api.GUI;
import dev.tigr.ares.core.gui.impl.accounts.AccountManagerGUI;
import dev.tigr.ares.core.util.render.Color;
import dev.tigr.ares.core.util.render.LocationIdentifier;

import static dev.tigr.ares.core.Ares.*;

/**
 * @author Makrennel 09/28/21
 * updated 11/30/21 by Tigermouthbear
 */
public class MainMenuGUI extends GUI {
    private static final LocationIdentifier LOGO = new LocationIdentifier("textures/logo/ares_logo_hex_transparent.png");

    public MainMenuGUI() {
        MenuButtonGroup menuButtonGroup = add(new MenuButtonGroup(this));
        menuButtonGroup.add(new MenuButton(this, "Singleplayer", () -> UTILS.openSinglePlayerMenu()));
        menuButtonGroup.add(new MenuButton(this, "Multiplayer", () -> UTILS.openMultiPlayerMenu()));
        menuButtonGroup.add(new MenuButton(this, "Accounts", () -> GUI_MANAGER.openGUI(AccountManagerGUI.class)));
        menuButtonGroup.add(new MenuButton(this, "Realms", () -> UTILS.openRealmsMenu()));
        menuButtonGroup.add(new MenuButton(this, "Options", () -> UTILS.openOptionsMenu()));
        menuButtonGroup.add(new MenuButton(this, "Quit", () -> UTILS.shutdown()));
        menuButtonGroup.setX(() -> getScreenWidth() / 3D * 2D - getScreenWidth() / 5D);
        menuButtonGroup.setY(() -> getScreenHeight() / 2D - menuButtonGroup.getHeight() / 2D);
        menuButtonGroup.setWidth(() -> getScreenWidth() / 5D);

        MenuButton customToggle = add(new MenuButton(this, "Minecraft Main Menu", () -> {
            ClickGUIMod.toggleCustomMenu();
            UTILS.openTitleScreen();
        }));
        customToggle.setWidth(() -> getScreenWidth() / 6D);
        customToggle.setHeight(() -> getScreenHeight() / 25D);
        customToggle.setX(() -> getScreenWidth() - customToggle.getWidth() - 2);
        customToggle.setY(() -> getScreenHeight() - customToggle.getHeight() - 2);
    }

    @Override
    public void draw(int mouseX, int mouseY, float partialTicks) {
        GUI_MANAGER.drawPanorama();
        super.draw(mouseX, mouseY, partialTicks);

        double size = getScreenHeight() / 4D;
        RENDERER.drawImage(getScreenWidth() / 3D - size, getScreenHeight() / 2D - size, size * 2, size * 2, LOGO, Color.WHITE);
    }
}
