package dev.tigr.ares.core.gui.impl.menu;

import dev.tigr.ares.core.gui.api.GUI;
import dev.tigr.ares.core.gui.impl.accounts.AccountManagerGUI;
import dev.tigr.ares.core.util.render.Color;
import dev.tigr.ares.core.util.render.LocationIdentifier;

import static dev.tigr.ares.core.Ares.*;

/**
 * @author Makrennel 09/28/21
 * updated 11/30/21 by Tigermouthbear
 */
public class SelectionMenuGUI extends GUI {
    private static final LocationIdentifier HELMET_FG = new LocationIdentifier("textures/logo/ares_logo_hex_fg.png");

    public SelectionMenuGUI() {
        MenuButtonGroup menuButtonGroup = add(new MenuButtonGroup(this));
        menuButtonGroup.add(new MenuButton(this, "SP", () -> UTILS.openSinglePlayerMenu()));
        menuButtonGroup.add(new MenuButton(this, "MP", () -> UTILS.openMultiPlayerMenu()));
        menuButtonGroup.add(new MenuButton(this, "AM", () -> GUI_MANAGER.openGUI(AccountManagerGUI.class)));
        menuButtonGroup.add(new MenuButton(this, "RL", () -> UTILS.openRealmsMenu()));
        menuButtonGroup.add(new MenuButton(this, "OP", () -> UTILS.openOptionsMenu()));
        menuButtonGroup.add(new MenuButton(this, "QT", () -> UTILS.shutdown()));
        menuButtonGroup.setX(() -> getScreenWidth() / 108D);
        menuButtonGroup.setY(() -> getScreenHeight() / 2D - menuButtonGroup.getHeight() / 2D);
        menuButtonGroup.setWidth(() -> getScreenWidth() / 27D);
    }

    @Override
    public void draw(int mouseX, int mouseY, float partialTicks) {
        double padding = getScreenWidth() / 18D;
        double helmetSize = padding / 3 * 2;
        double helmetOffset = padding / 6D;
        double right = getScreenWidth() - padding;
        RENDERER.drawRect(0, 0, padding, getScreenHeight(), Color.ARES_GRAY);
        RENDERER.drawRect(right, 0, padding, getScreenHeight(), Color.ARES_GRAY);
        RENDERER.drawLine(padding, 0, padding, getScreenHeight(), 2, Color.ARES_RED);
        RENDERER.drawLine(right, 0, right, getScreenHeight(), 2, Color.ARES_RED);
        RENDERER.drawImage(right + helmetOffset, getScreenHeight() - helmetSize - helmetOffset, helmetSize, helmetSize, HELMET_FG, Color.ARES_RED);

        super.draw(mouseX, mouseY, partialTicks);
    }
}
