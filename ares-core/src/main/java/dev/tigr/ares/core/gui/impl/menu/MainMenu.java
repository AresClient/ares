package dev.tigr.ares.core.gui.impl.menu;

import dev.tigr.ares.core.feature.module.ClickGUIMod;
import dev.tigr.ares.core.gui.impl.accounts.AccountManagerGUI;
import dev.tigr.ares.core.util.render.Color;
import dev.tigr.ares.core.util.render.LocationIdentifier;

import static dev.tigr.ares.core.Ares.*;

/**
 * @author Makrennel 09/28/21
 */
public class MainMenu extends MenuElement {
    private static final LocationIdentifier HELMET = new LocationIdentifier("textures/logo/ares_helmet_white.png");
    private static final LocationIdentifier HELMET2 = new LocationIdentifier("textures/logo/ares_logo_white.png");

    Color backgroundColor;
    MenuButtonGroup menuButtonGroup;

    MenuButton customToggle;
    MenuButton buttonToggle;

    int scale, groupX, groupY, groupW, groupH, groupS, imgS, imgX, imgY;

    public MainMenu(int screenWidth, int screenHeight, Color backgroundColor, int scale) {
        if(scale == 0) scale = 3;
        this.backgroundColor = backgroundColor;
        setWidth(screenWidth);
        setHeight(screenHeight);

        this.scale = scale;

        groupW = 40 *scale;
        groupH = 10 *scale;
        groupS = (int)(3.5 *scale);
        groupY = (screenHeight /2) -(groupH *3) -(int)(groupS *2.5);

        imgS = (groupH *6) + (groupS *5);
        imgX = 3 *(screenWidth /4) -((40 *scale) /2) -(int)((imgS /3) *0.707);
        imgY = groupY -(int)(screenHeight *0.012963) +(int)((screenHeight *0.074074) /2);

        groupX = screenWidth /4 -((40 *scale) /2);


        if(groupX < 0) groupX = 0;
        if(groupY < 0) groupY = 0;

        menuButtonGroup = new MenuButtonGroup(
                this, MenuButtonGroup.GroupDirection.VERTICAL, groupX, groupY, groupW, groupH, groupS,
                new MenuButton("Singleplayer"), new MenuButton("Multiplayer"), new MenuButton("Account Manager"),
                new MenuButton("Realms"), new MenuButton("Options"), new MenuButton("Quit")
        );

        customToggle = new MenuButton("Custom Menu");
        customToggle.setPressed(ClickGUIMod.INSTANCE.customMenu.getValue());
        customToggle.setOnOffSwitch(true);
        customToggle.setX(getWidth() -(18 *scale));
        customToggle.setY(4);
        customToggle.setWidth(12 *scale);
        customToggle.setHeight(12 *scale);

        buttonToggle = new MenuButton("Button Toggle");
        buttonToggle.setPressed(ClickGUIMod.INSTANCE.customButtons.getValue());
        buttonToggle.setOnOffSwitch(true);
        buttonToggle.setX(getWidth() -(18 *scale));
        buttonToggle.setY(4 +12 *scale);
        buttonToggle.setWidth(12 *scale);
        buttonToggle.setHeight(12 *scale);
    }

    public void draw(int mouseX, int mouseY, float partialTicks) {
        if(customToggle.isPressed()) {
            RENDERER.drawRect(getRenderX(), getRenderY(), getWidth(), getHeight(), backgroundColor);
            RENDERER.drawLine(getRenderX() + (getWidth() / 2D), getRenderY(), getRenderX() + (getWidth() / 2D), getRenderY() + getHeight(), 2, Color.ARES_RED);
            RENDERER.drawImage(imgX, imgY, imgS, imgS, HELMET2, Color.ARES_RED);
            menuButtonGroup.draw(mouseX, mouseY, partialTicks);
        }

        customToggle.draw(mouseX, mouseY, partialTicks);
        buttonToggle.draw(mouseX, mouseY, partialTicks);
    }

    public MenuButtonGroup getMenuButtonGroup() {
        return menuButtonGroup;
    }

    public MenuButton getCustomToggle() {
        return customToggle;
    }

    public void onClick(double mouseX, double mouseY) {
        customToggle.onClick(mouseX, mouseY);

        if(customToggle.isMouseOver(mouseX, mouseY)) ClickGUIMod.INSTANCE.customMenu.setValue(!ClickGUIMod.INSTANCE.customMenu.getValue());

        if(customToggle.isPressed()) {
            menuButtonGroup.onClick(mouseX, mouseY);

            for(MenuButton button : menuButtonGroup.getButtons()) {
                if(button.isMouseOver(mouseX, mouseY)) {
                    if(button.getButtonName().equals("Account Manager")) GUI_MANAGER.openGUI(AccountManagerGUI.class);
                    if(button.getButtonName().equals("Quit")) UTILS.shutdown();
                }
            }
        }

        buttonToggle.onClick(mouseX, mouseY);

        if(buttonToggle.isMouseOver(mouseX, mouseY)) ClickGUIMod.INSTANCE.customButtons.setValue(!ClickGUIMod.INSTANCE.customButtons.getValue());
    }
}
