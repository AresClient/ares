package org.aresclient.ares.api.minecraft.render;

import org.aresclient.ares.AresStatics;

public class Screen {
    private final String title;
    private int width;
    private int height;

    public Screen(String title) {
        this.title = title;
    }

    public void update() {
    }

    public void render(int mouseX, int mouseY, float delta) {
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
        this.width = width;
        this.height = height;
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

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public static void openChatScreen(String input) {
        AresStatics.openChatScreen(input);
    }

    public static void openDemoScreen() {
        AresStatics.openDemoScreen();
    }

    public static void openMultiplayerScreen() {
        AresStatics.openMultiplayerScreen();
    }

    public static void openOptionsScreen() {
        AresStatics.openOptionsScreen();
    }

    public static void openSelectWorldScreen() {
        AresStatics.openSelectWorldScreen();
    }

    public static void openRealmsMainScreen() {
        AresStatics.openRealmsMainScreen();
    }

    public static void openTitleScreen() {
        AresStatics.openTitleScreen();
    }
}
