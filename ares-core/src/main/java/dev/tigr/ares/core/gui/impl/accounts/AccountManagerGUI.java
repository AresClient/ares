package dev.tigr.ares.core.gui.impl.accounts;

import dev.tigr.ares.core.feature.AccountManager;
import dev.tigr.ares.core.gui.api.Element;
import dev.tigr.ares.core.gui.api.GUI;
import dev.tigr.ares.core.util.AbstractAccount;
import dev.tigr.ares.core.util.render.Color;

import java.util.ArrayList;

import static dev.tigr.ares.core.Ares.*;

/**
 * GUI for interfacing with the account manager
 *
 * @author Tigermouthbear 7/7/20
 */
public class AccountManagerGUI extends GUI {
    private final AccountsScrollPlane accountsScrollPlane;
    private AccountElement prev = null;
    private final LoginElement loginElement;

    public AccountManagerGUI() {
        // create scroll plane
        accountsScrollPlane = new AccountsScrollPlane(this);
        accountsScrollPlane.setY(() -> getScreenHeight() / 8d);
        accountsScrollPlane.setX(() -> getScreenWidth() / 3d);
        accountsScrollPlane.setWidth(() -> getScreenWidth() / 3d);
        accountsScrollPlane.setHeight(() -> getScreenHeight() * 0.7d);
        add(accountsScrollPlane);

        // add all accounts
        for(AbstractAccount account: AccountManager.getAccounts()) addAccount(account);

        // create element
        loginElement = new LoginElement(this);
        loginElement.setX(() -> getScreenWidth() / 3d);
        loginElement.setY(() -> getScreenHeight() - loginElement.getHeight() - getScreenHeight() / 20);
        loginElement.setHeight(() -> getScreenHeight() / 10d);
        loginElement.setWidth(() -> getScreenWidth() / 3d);
        add(loginElement);
    }

    @Override
    public void draw(int mouseX, int mouseY, float partialTicks) {
        GUI_MANAGER.drawBackground();

        // calculation positions
        double x = getScreenWidth() / 3.5d;
        double y = getScreenHeight() / 30d;
        double width = getScreenWidth() - (getScreenWidth() / 3.5d) * 2;
        double height = getScreenHeight() - getScreenHeight() / 15d;

        // draw center thingy
        RENDERER.drawRect(x, y, width, height, Color.BLACK);

        // draw outline
        RENDERER.drawLineLoop(
                1,
                Color.WHITE,
                x, y,
                x, y + height,
                x + width, y + height,
                x + width, y
        );

        // draw title
        double titleHeight = getScreenHeight() / 30d;
        double titleWidth = FONT_RENDERER.getStringWidth("Account Manager", titleHeight);
        FONT_RENDERER.drawStringWithCustomHeight("Account Manager", getScreenWidth() / 2d - titleWidth / 2, y + titleHeight, Color.WHITE, titleHeight);

        super.draw(mouseX, mouseY, partialTicks);
    }

    // do this to prevent concurrent modification exception
    @Override
    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        new ArrayList<>(getElements()).stream().filter(Element::isVisible).forEach(element -> element.click(mouseX, mouseY, mouseButton));
    }

    // adds account to gui
    public void addAccount(AbstractAccount account) {
        AccountElement accountElement = new AccountElement(this, account);
        accountElement.setWidth(() -> getScreenWidth() / 3d);
        accountElement.setHeight(() -> getScreenHeight() / 15d);

        if(prev != null) {
            AccountElement finalPrev = prev;
            accountElement.setY(() -> finalPrev.getY() + finalPrev.getHeight() + getScreenHeight() / 70);
        }
        prev = accountElement;

        accountsScrollPlane.add(accountElement);
    }

    public void refreshAccounts() {
        // remove all accounts and reset list pos
        accountsScrollPlane.getChildren().removeIf(element -> element instanceof AccountElement);
        prev = null;

        // add all accounts back
        for(AbstractAccount a: AccountManager.getAccounts()) addAccount(a);
    }
}
