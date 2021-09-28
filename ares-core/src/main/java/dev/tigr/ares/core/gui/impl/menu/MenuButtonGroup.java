package dev.tigr.ares.core.gui.impl.menu;

import dev.tigr.ares.CoreWrapper;

import java.util.Arrays;
import java.util.List;

/**
 * @author Makrennel 09/28/21
 */
public class MenuButtonGroup extends MenuElement implements CoreWrapper {
    private final List<MenuButton> buttons;
    private final GroupDirection groupDirection;
    int buttonWidth, buttonHeight, buttonSpacing;

    public List<MenuButton> getButtons() {
        return buttons;
    }

    public MenuButtonGroup(MenuElement parent, GroupDirection groupDirection, int x, int y, int buttonWidth, int buttonHeight, int buttonSpacing, MenuButton... buttons) {
        super(parent);
        this.groupDirection = groupDirection;
        this.setX(x);
        this.setY(y);
        this.buttonWidth = buttonWidth;
        this.buttonHeight = buttonHeight;
        this.buttonSpacing = buttonSpacing;
        this.buttons = Arrays.asList(buttons);
        for(MenuButton button: this.buttons) {
            button.setWidth(this.buttonWidth);
            button.setHeight(this.buttonHeight);
            button.setParent(this);
        }
    }

    public void draw(int mouseX, int mouseY, float partialTicks) {
        for(MenuButton button: buttons) {
            if(groupDirection == GroupDirection.VERTICAL) {
                int iVal = buttons.indexOf(button);
                int yVal = (iVal *buttonHeight) +(iVal *buttonSpacing);

                button.setX(0);
                button.setY(yVal);
            } else {
                int iVal = buttons.indexOf(button);
                int xVal = (iVal *buttonWidth) +(iVal *buttonSpacing);

                button.setX(xVal);
                button.setY(0);
            }
            button.draw(mouseX, mouseY, partialTicks);
        }
    }

    public void onClick(double mouseX, double mouseY) {
        for(MenuButton button: buttons) {
            if(button.isMouseOver(mouseX, mouseY)) {
                buttons.forEach(b -> {
                    if(b.isPressed()) b.setPressed(false);
                });
                button.onClick(mouseX, mouseY);
            }
        }
    }

    public void addButton(MenuButton button) {
        button.setWidth(this.buttonWidth);
        button.setHeight(this.buttonHeight);
        button.setParent(this);
        buttons.add(button);
    }

    public MenuButton getButtonByName(String name) {
        for(MenuButton button: buttons) {
            if(button.getButtonName().equals(name)) return button;
        }

        return null;
    }

    public enum GroupDirection { HORIZONTAL, VERTICAL }
}
