package dev.tigr.ares.core.util;

import com.mojang.authlib.exceptions.AuthenticationException;
import dev.tigr.ares.core.util.global.MojangApi;

import java.io.IOException;

/**
 * @author Tigermouthbear 11/24/20
 * basic functions all account objects should have and which are used in {@link dev.tigr.ares.core.gui.impl.accounts.AccountManagerGUI}
 */
public abstract class AbstractAccount {
    /**
     * Stores the email for the account (username for legacy accounts)
     */
    protected final String email;

    /**
     * Stores the password for the account
     */
    protected final String password;

    /**
     * Stores the unique id of the account
     */
    protected final String uuid;

    /**
     * Stores the username of the account
     */
    protected final String name;

    /**
     * Creates a new account with the provided email, password, and uuid
     *
     * @param email    email of account (username for legacy accounts)
     * @param password password of account
     * @param uuid     uuid of account
     */
    public AbstractAccount(String email, String password, String uuid) throws IOException {
        this.email = email;
        this.password = password;
        this.uuid = uuid;
        this.name = MojangApi.getUsername(uuid);
    }

    /**
     * Creates a new account with the provided email and password
     *
     * @param email    email of account
     * @param password password of account
     * @throws AuthenticationException thrown if email and password don't match
     */
    public AbstractAccount(String email, String password) throws AuthenticationException, IOException {
        this.email = email;
        this.password = password;
        this.uuid = getUUID(email, password);
        this.name = MojangApi.getUsername(uuid);
    }

    /**
     * Gets uuid of email and password combination
     *
     * @param email    email of account
     * @param password password of account
     * @return uuid of account as string without dashes
     * @throws AuthenticationException thrown if email and password don't match
     */
    protected abstract String getUUID(String email, String password) throws AuthenticationException;

    /**
     * Logins to account by yggdrasil authentication
     *
     * @return if login was successful
     * @throws AuthenticationException called if there is an error while authenticating
     */
    public abstract boolean login() throws AuthenticationException;

    /**
     * Checks if the current user is equal to this account
     *
     * @return whether this is the account in use
     */
    public abstract boolean isLoggedIn();

    /**
     * Draws the head of the account
     *
     * @param x      x position of the head
     * @param y      y position of the head
     * @param width  width of the head
     * @param height height of the head
     */
    public abstract void drawHead(double x, double y, double width, double height);

    /**
     * Getter for the accounts email
     *
     * @return email of account
     */
    public String getEmail() {
        return email;
    }

    /**
     * Getter for the password of the account
     *
     * @return password of the account
     */
    public String getPassword() {
        return password;
    }

    /**
     * Getter for the uuid of the account
     *
     * @return uuid of the account
     */
    public String getUuid() {
        return uuid;
    }

    /**
     * Getter for the name of the account
     *
     * @return name of the account
     */
    public String getName() {
        return name;
    }
}
