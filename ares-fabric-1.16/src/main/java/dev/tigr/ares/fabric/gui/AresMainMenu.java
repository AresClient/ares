package dev.tigr.ares.fabric.gui;

import dev.tigr.ares.core.Ares;
import dev.tigr.ares.core.gui.impl.accounts.AccountManagerGUI;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.LiteralText;

/**
 * @author Tigermouthbear 8/27/20
 */
public class AresMainMenu extends TitleScreen {
    @Override
    public void init() {
        super.init();
        addButton(new ButtonWidget(2, 2, 98, 20, new LiteralText("Account Manager"), (buttonWidget) -> Ares.GUI_MANAGER.openGUI(AccountManagerGUI.class)));
    }
}
