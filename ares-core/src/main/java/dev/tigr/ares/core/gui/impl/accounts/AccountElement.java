package dev.tigr.ares.core.gui.impl.accounts;

import com.mojang.authlib.exceptions.AuthenticationException;
import dev.tigr.ares.core.feature.AccountManager;
import dev.tigr.ares.core.gui.api.Element;
import dev.tigr.ares.core.gui.api.GUI;
import dev.tigr.ares.core.util.AbstractAccount;
import dev.tigr.ares.core.util.render.Color;

import static dev.tigr.ares.core.Ares.FONT_RENDERER;
import static dev.tigr.ares.core.Ares.RENDERER;

/**
 * @author Tigermouthbear 7/7/20
 */
public class AccountElement extends Element {
    private final AbstractAccount account;

    public AccountElement(GUI gui, AbstractAccount account) {
        super(gui);

        this.account = account;
    }

    @Override
    public void draw(int mouseX, int mouseY, float partialTicks) {
        super.draw(mouseX, mouseY, partialTicks);

        // draw head
        account.drawHead(getRenderX(), getRenderY(), getHeight(), getHeight());

        FONT_RENDERER.drawStringWithCustomHeight(account.getName(), getRenderX() + getHeight() + 2, getRenderY(), Color.WHITE, getHeight() / 2d);
        if(account.isLoggedIn())
            FONT_RENDERER.drawStringWithCustomHeight("Connected", getRenderX() + getHeight() + 2, getRenderY() + getHeight() / 2d, Color.GREEN, getHeight() / 2d);

        // draw delete sign
        if(!account.isLoggedIn()) {
            RENDERER.drawLine(
                    getRenderX() + getWidth() - getHeight() / 2d,
                    getRenderY() + getHeight() / 2d,
                    getRenderX() + getWidth(),
                    getRenderY() + getHeight() / 2d,
                    6,
                    Color.WHITE
            );
        }
    }

    @Override
    public void click(int mouseX, int mouseY, int mouseButton) {
        if(isMouseOver(mouseX, mouseY) && mouseButton == 0) {
            if(mouseX > getRenderX() + getWidth() - getHeight() && !account.isLoggedIn()) {
                AccountManager.getAccounts().remove(account);
                ((AccountManagerGUI) getGUI()).refreshAccounts();
            } else {
                try {
                    account.login();
                } catch(AuthenticationException e) {
                    e.printStackTrace();
                }
            }
        }

        super.click(mouseX, mouseY, mouseButton);
    }
}
