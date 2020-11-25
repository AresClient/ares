package dev.tigr.ares.fabric.impl.util;

import dev.tigr.ares.core.gui.api.GUI;
import dev.tigr.ares.core.util.IGUIManager;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.LiteralText;

import java.util.HashMap;
import java.util.Map;

import static dev.tigr.ares.Wrapper.MC;

/**
 * @author Tigermoutbear 11/23/20
 * manages gui rendering, input, and opening
 */
public class CustomGUIManager extends Screen implements IGUIManager {
    private final Map<Class<? extends GUI>, GUI> instanceMap = new HashMap<>();
    private GUI gui;

    public CustomGUIManager() {
        super(new LiteralText("Ares GUI"));
    }

    @Override
    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        gui.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int mouseButton) {
        gui.mouseClicked((int) mouseX, (int) mouseY, mouseButton);
        return super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int mouseButton) {
        gui.mouseReleased((int) mouseX, (int) mouseY, mouseButton);
        return super.mouseReleased(mouseX, mouseY, mouseButton);
    }

    @Override
    public boolean charTyped(char character, int keycode) {
        if(keycode == 27) onClose();
        gui.keyTyped(character, keycode);
        return super.charTyped(character, keycode);
    }

    @Override
    public boolean keyPressed(int keycode, int something, int mods) {
        gui.keyTyped(null, keycode);
        return super.keyPressed(keycode, something, mods);
    }

    @Override
    public void onClose() {
        super.onClose();
        gui.onGuiClosed();
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void drawBackground() {
        renderBackground(new MatrixStack());
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
        return getZOffset();
    }

    @Override
    public boolean isEnterKey(int keycode) {
        return keycode == 257;
    }

    @Override
    public boolean isBackKey(int keycode) {
        return keycode == 259;
    }

    @Override
    public boolean isChatAllowed(char chr) {
        return true;
    }

    @Override
    public void openGUI(Class<? extends GUI> clazz) {
        set(clazz);
        MC.openScreen(this);
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