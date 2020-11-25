package dev.tigr.ares.forge.gui;

import dev.tigr.ares.core.Ares;
import dev.tigr.ares.core.gui.impl.accounts.AccountManagerGUI;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiMainMenu;

import java.io.IOException;

/**
 * @author Tigermouthbear 7/7/20
 */
public class AresMainMenu extends GuiMainMenu {
    @Override
    public void initGui() {
        super.initGui();

        buttonList.add(new GuiButton(69, 2, 2, 98, 20, "Account Manager"));
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        if(button.id == 69) Ares.GUI_MANAGER.openGUI(AccountManagerGUI.class);
        else super.actionPerformed(button);
    }
}
