package dev.tigr.ares.core.util;

import com.mojang.authlib.exceptions.AuthenticationException;

import java.io.IOException;

/**
 * @author Tigermouthbear 11/23/20
 * basic utils used by core which need to be implemented for every version
 */
public interface IUtils {
    void printMessage(String message);

    void executeBaritoneCommand(String string);

    String getPlayerName();

    void openHUDEditor();

    void openTitleScreen();
    void openSinglePlayerMenu();
    void openMultiPlayerMenu();
    void openRealmsMenu();
    void openOptionsMenu();

    AbstractAccount createAccount(String email, String password, String uuid) throws IOException;
    AbstractAccount createAccount(String email, String password) throws IOException, AuthenticationException;

    void shutdown();

    float getRenderPartialTicks();

    float getTickLength();

    void setTickLength(float tickLength);

    default void setTpsMultiplier(float tps) {
        setTickLength(1000 / (tps * 20));
    }
}
