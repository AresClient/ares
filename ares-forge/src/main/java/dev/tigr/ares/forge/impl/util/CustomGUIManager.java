package dev.tigr.ares.forge.impl.util;

import dev.tigr.ares.core.gui.api.GUI;
import dev.tigr.ares.core.util.IGUIManager;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.util.ChatAllowedCharacters;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static dev.tigr.ares.Wrapper.MC;

/**
 * @author Tigermoutbear 11/23/20
 * manages gui rendering, input, and opening
 */
public class CustomGUIManager extends GuiScreen implements IGUIManager {
    private final Map<Class<? extends GUI>, GUI> instanceMap = new HashMap<>();
    private GUI gui;

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        // do scroll
        if(Mouse.hasWheel()) gui.mouseScrolled(mouseX, mouseY, Mouse.getDWheel());

        gui.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        gui.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    public void mouseReleased(int mouseX, int mouseY, int mouseButton) {
        gui.mouseReleased(mouseX, mouseY, mouseButton);
    }

    @Override
    public void keyTyped(char typedChar, int keyCode) throws IOException {
        gui.keyTyped(typedChar, keyCode);

        super.keyTyped(typedChar, keyCode);
    }

    @Override
    public void onGuiClosed() {
        gui.onGuiClosed();
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }

    @Override
    public void drawBackground() {
        drawDefaultBackground();
    }

    @Override
    public int getWidth() {
        return width;
    }

    @Override
    public int getHeight() {
        return height;
    }

    @Override
    public float getZLevel() {
        return zLevel;
    }

    @Override
    public boolean isEnterKey(int keycode) {
        return keycode == Keyboard.KEY_RETURN;
    }

    @Override
    public boolean isBackKey(int keycode) {
        return keycode == Keyboard.KEY_BACK;
    }

    @Override
    public boolean isChatAllowed(char chr) {
        return ChatAllowedCharacters.isAllowedCharacter(chr);
    }

    @Override
    public void openGUI(Class<? extends GUI> clazz) {
        set(clazz);
        MC.displayGuiScreen(this);
    }

    private void set(Class<? extends GUI> clazz) {
        gui = instanceMap.get(clazz);
        if(gui == null) {
            try {
                gui = clazz.newInstance();
            } catch(InstantiationException | IllegalAccessException e) {
                e.printStackTrace();
            }
            instanceMap.put(clazz, gui);
        }
    }
}
