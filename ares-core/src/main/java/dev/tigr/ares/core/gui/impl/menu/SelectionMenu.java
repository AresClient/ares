package dev.tigr.ares.core.gui.impl.menu;

import dev.tigr.ares.core.feature.module.ClickGUIMod;
import dev.tigr.ares.core.gui.impl.accounts.AccountManagerGUI;
import dev.tigr.ares.core.util.render.Color;
import dev.tigr.ares.core.util.render.LocationIdentifier;

import static dev.tigr.ares.core.Ares.*;

/**
 * @author Makrennel 09/28/21
 */
public class SelectionMenu extends MenuElement {
    private static final LocationIdentifier HELMET = new LocationIdentifier("textures/logo/ares_helmet_white.png");
    private static final LocationIdentifier HELMET2 = new LocationIdentifier("textures/logo/ares_logo_white.png");

    MenuButtonGroup menuButtonGroup;

    MenuButton customToggle;
    MenuButton buttonToggle;

    int scale, groupX, groupY, groupW, groupH, groupS, imgX;

    public SelectionMenu(int screenWidth, int screenHeight, int scale) {
        if(scale == 0) scale = 3;
        setWidth(screenWidth);
        setHeight(screenHeight);

        this.scale = scale;

        groupW = 18 *scale;
        groupH = 10 *scale;
        groupS = (int)(3.5 *scale);
        groupY = (screenHeight /2) -(groupH *3) -(int)(groupS *2.5);

        imgX = screenWidth -groupW -4;

        groupX = 4;

        if(groupX < 0) groupX = 0;
        if(groupY < 0) groupY = 0;

        menuButtonGroup = new MenuButtonGroup(
                this, MenuButtonGroup.GroupDirection.VERTICAL, groupX, groupY, groupW, groupH, groupS,
                new MenuButton("SP"), new MenuButton("MP"), new MenuButton("AM"),
                new MenuButton("RL"), new MenuButton("OP"), new MenuButton("QT")
        );

        customToggle = new MenuButton("Custom Menu");
        customToggle.setPressed(ClickGUIMod.INSTANCE.customMenu.getValue());
        customToggle.setOnOffSwitch(true);
        customToggle.setX(getWidth() -groupW);
        customToggle.setY(4);
        customToggle.setWidth(12 *scale);
        customToggle.setHeight(12 *scale);

        buttonToggle = new MenuButton("Button Toggle");
        buttonToggle.setPressed(ClickGUIMod.INSTANCE.customButtons.getValue());
        buttonToggle.setOnOffSwitch(true);
        buttonToggle.setX(getWidth() -groupW);
        buttonToggle.setY(4 +12 *scale);
        buttonToggle.setWidth(12 *scale);
        buttonToggle.setHeight(12 *scale);
    }

    public void draw(int mouseX, int mouseY, float partialTicks) {
        if(customToggle.isPressed()) {
            RENDERER.drawRect(getRenderX(), getRenderY(), menuButtonGroup.buttonWidth +8, getHeight(), new Color(0,0,0,1));
            RENDERER.drawRect(getRenderX() +getWidth() -groupW -8, getRenderY(), menuButtonGroup.buttonWidth +8, getHeight(), new Color(0,0,0,1));
            RENDERER.drawLine(getRenderX() +menuButtonGroup.buttonWidth +8, getRenderY(), getRenderX() + menuButtonGroup.buttonWidth +8, getRenderY() + getHeight(), 2, Color.ARES_RED);
            RENDERER.drawLine(getRenderX() +getWidth() -groupW -8, getRenderY(), getRenderX() +getWidth() -groupW -8, getRenderY() + getHeight(), 2, Color.ARES_RED);
            RENDERER.drawImage(getRenderX() +getWidth() -groupW -4, getHeight() -groupW -6, groupW, groupW, HELMET, Color.ARES_RED);
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
                    if(button.getButtonName().equals("AM")) GUI_MANAGER.openGUI(AccountManagerGUI.class);
                    if(button.getButtonName().equals("QT")) UTILS.shutdown();
                }
            }
        }

        buttonToggle.onClick(mouseX, mouseY);

        if(buttonToggle.isMouseOver(mouseX, mouseY)) ClickGUIMod.INSTANCE.customButtons.setValue(!ClickGUIMod.INSTANCE.customButtons.getValue());
    }
}
