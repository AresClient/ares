package dev.tigr.ares.core.util;

import dev.tigr.ares.core.gui.api.GUI;

/**
 * @author Tigermouthbear 11/23/20
 */
public interface IGUIManager {
    void openGUI(Class<? extends GUI> clazz);

    void drawBackground();

    int getWidth();

    int getHeight();

    float getZLevel();

    boolean isEnterKey(int keycode);

    boolean isBackKey(int keycode);

    boolean isPasteKey(int keycode);

    String getClipboardText();

    boolean isChatAllowed(char chr);
}
