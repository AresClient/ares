package org.aresclient.ares.api.util;

import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.gui.screen.DemoScreen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import net.minecraft.client.gui.screen.option.OptionsScreen;
import net.minecraft.client.gui.screen.world.SelectWorldScreen;
import net.minecraft.client.realms.gui.screen.RealmsMainScreen;
import org.aresclient.ares.Main;

public class Screen {
    //TODO: Merge this with ScreenAdapter?
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
        Main.getMC().setScreen(new ChatScreen(input));
    }

    public static void openDemoScreen() {
        Main.getMC().setScreen(new DemoScreen());
    }

    public static void openMultiplayerScreen() {
        Main.getMC().setScreen(new MultiplayerScreen(Main.getMC().currentScreen));
    }

    public static void openOptionsScreen() {
        Main.getMC().setScreen(new OptionsScreen(Main.getMC().currentScreen, Main.getMC().options));
    }

    public static void openSelectWorldScreen() {
        Main.getMC().setScreen(new SelectWorldScreen(Main.getMC().currentScreen));
    }

    public static void openRealmsMainScreen() {
        Main.getMC().setScreen(new RealmsMainScreen(Main.getMC().currentScreen));
    }

    public static void openTitleScreen() {
        Main.getMC().setScreen(new TitleScreen());
    }
}
