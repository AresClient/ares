package dev.tigr.ares.core.util;

import com.mojang.authlib.exceptions.AuthenticationException;
import dev.tigr.ares.core.util.render.font.AbstractFontRenderer;
import dev.tigr.ares.core.util.render.font.GlyphFont;

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

    AbstractAccount createAccount(String email, String password, String uuid) throws IOException;

    AbstractAccount createAccount(String email, String password) throws IOException, AuthenticationException;
}
