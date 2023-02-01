package org.aresclient.ares.renderer;

import org.aresclient.ares.Ares;
import org.aresclient.ares.api.IMinecraft;
import org.aresclient.ares.api.ScreenContext;

public class Screen implements ScreenContext {
    private ScreenContext context = null;
    private final String title;

    public Screen(String title) {
        this.title = title;
    }

    public void init() {
    }

    public void render(int mouseX, int mouseY, float partialTicks) {
    }

    public void click(int mouseX, int mouseY, int mouseButton) {
    }

    public void release(int mouseX, int mouseY, int mouseButton) {
    }

    public void type(Character typedChar, int keyCode) {
    }

    public void scroll(int mouseX, int mouseY, double value) {
    }

    // TODO: THIS ISNT CONSISTENT ACROSS VERSIONS
    public void resize(int width, int height) {
    }

    public void close() {
    }

    public boolean shouldCloseOnEsc() {
        return true;
    }

    public boolean shouldPause() {
        return true;
    }

    public String getTitle() {
        return title;
    }

    public void setContext(ScreenContext context) {
        this.context = context;
    }

    @Override
    public int getWidth() {
        return context.getWidth();
    }

    @Override
    public void setWidth(int width) {
        context.setWidth(width);
    }

    @Override
    public int getHeight() {
        return context.getHeight();
    }

    @Override
    public void setHeight(int height) {
        context.setHeight(height);
    }

    public static void openChatScreen(String input) {
        Ares.INSTANCE.creator.openChatScreen(input);
    }

    public static void openDemoScreen() {
        Ares.INSTANCE.creator.openDemoScreen();
    }

    public static void openMultiplayerScreen() {
        Ares.INSTANCE.creator.openMultiplayerScreen();
    }

    public static void openOptionsScreen() {
        Ares.INSTANCE.creator.openOptionsScreen();
    }

    public static void openSelectWorldScreen() {
        Ares.INSTANCE.creator.openSelectWorldScreen();
    }

    public static void openRealmsMainScreen() {
        Ares.INSTANCE.creator.openRealmsMainScreen();
    }

    public static void openTitleScreen() {
        Ares.INSTANCE.creator.openTitleScreen();
    }
}
