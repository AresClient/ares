package dev.tigr.ares.core.gui.impl.accounts;

import com.mojang.authlib.exceptions.AuthenticationException;
import dev.tigr.ares.core.feature.AccountManager;
import dev.tigr.ares.core.gui.api.Element;
import dev.tigr.ares.core.gui.api.GUI;
import dev.tigr.ares.core.gui.api.TextField;
import dev.tigr.ares.core.util.AbstractAccount;
import dev.tigr.ares.core.util.render.Color;

import java.io.IOException;

import static dev.tigr.ares.core.Ares.FONT_RENDERER;
import static dev.tigr.ares.core.Ares.UTILS;

/**
 * Login pane for accounts manager GUI
 *
 * @author Tigermouthbear 7/8/20
 */
public class LoginElement extends Element {
    private final TextField email;
    private final TextField password;

    public LoginElement(GUI gui) {
        super(gui);

        email = new TextField(getGUI());
        email.setX(() -> FONT_RENDERER.getStringWidth("Password: ", getRowHeight()));
        email.setY(() -> getRowHeight() * 0.2);
        email.setHeight(() -> getRowHeight() * 0.8);
        email.setWidth(() -> getWidth() - FONT_RENDERER.getStringWidth("Password: ", getRowHeight()));
        add(email);

        password = new TextField(getGUI());
        password.setX(() -> FONT_RENDERER.getStringWidth("Password: ", getRowHeight()));
        password.setY(() -> getRowHeight() + getRowHeight() * 0.2);
        password.setHeight(() -> getRowHeight() * 0.8);
        password.setWidth(() -> getWidth() - (int) FONT_RENDERER.getStringWidth("Password: ", getRowHeight()));
        add(password);
    }

    @Override
    public void draw(int mouseX, int mouseY, float partialTicks) {
        super.draw(mouseX, mouseY, partialTicks);

        FONT_RENDERER.drawStringWithCustomHeight("Email: ", getRenderX(), getRenderY(), Color.WHITE, getRowHeight());
        FONT_RENDERER.drawStringWithCustomHeight("Password: ", getRenderX(), getRenderY() + getRowHeight(), Color.WHITE, getRowHeight());

        double width = FONT_RENDERER.getStringWidth("Login", getRowHeight());
        FONT_RENDERER.drawStringWithCustomHeight("Login", getRenderX() + getWidth() / 2d - width / 2, getRenderY() + getRowHeight() * 2, Color.WHITE, getRowHeight());
    }

    @Override
    public void click(int mouseX, int mouseY, int mouseButton) {
        super.click(mouseX, mouseY, mouseButton);

        // login if click the login button
        if(isMouseOver(mouseX, mouseY) && mouseY > getRenderY() + getHeight() - getRowHeight() && mouseButton == 0) {
            AbstractAccount account;
            try {
                account = UTILS.createAccount(email.getText(), password.getText());
            } catch(AuthenticationException | IOException e) {
                return;
            }

            AccountManager.getAccounts().add(account);
            ((AccountManagerGUI) getGUI()).addAccount(account);

            // reset text boxes
            email.setText("");
            password.setText("");
        }
    }

    private double getRowHeight() {
        return getHeight() / 3d;
    }
}
