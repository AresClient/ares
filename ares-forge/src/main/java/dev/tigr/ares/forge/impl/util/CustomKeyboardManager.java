package dev.tigr.ares.forge.impl.util;

import dev.tigr.ares.core.util.IKeyboardManager;
import org.lwjgl.input.Keyboard;

/**
 * @author Tigermouthbear 11/24/20
 */
public class CustomKeyboardManager implements IKeyboardManager {
    @Override
    public String getKeyName(int key) {
        return Keyboard.getKeyName(key);
    }
}
