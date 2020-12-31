package dev.tigr.ares.core.gui.impl.game;

import dev.tigr.ares.core.feature.module.ClickGUIMod;
import dev.tigr.ares.core.gui.api.GUI;
import dev.tigr.ares.core.gui.impl.game.nav.NavigationBar;
import dev.tigr.ares.core.gui.impl.game.nav.NavigationButton;
import dev.tigr.ares.core.gui.impl.game.window.Window;
import dev.tigr.ares.core.gui.impl.game.window.windows.ConsoleWindow;
import dev.tigr.ares.core.gui.impl.game.window.windows.WelcomeWindow;
import dev.tigr.ares.core.gui.impl.game.window.windows.modules.CompactModulesWindow;
import dev.tigr.ares.core.gui.impl.game.window.windows.modules.ExpandedModulesWindow;
import dev.tigr.ares.core.setting.SettingCategory;
import dev.tigr.ares.core.util.function.DynamicValue;
import dev.tigr.ares.core.util.render.Color;
import dev.tigr.ares.core.util.render.LocationIdentifier;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static dev.tigr.ares.core.Ares.UTILS;

/**
 * @author Tigermouthbear 6/16/20
 */
public class ClickGUI extends GUI {
    public static final SettingCategory SETTING_CATEGORY = new SettingCategory("GUI");
    private static final DynamicValue<Color> COLOR = ClickGUIMod::getColor;

    private final List<Window> windows = new ArrayList<>();

    public ClickGUI() {
        NavigationBar navigationBar = add(new NavigationBar(this, COLOR));

        // create modules window
        CompactModulesWindow modules = new CompactModulesWindow(this, COLOR);
        navigationBar.addNavigationButton(new NavigationButton(this, new LocationIdentifier("textures/icons/modules.png"), () -> toggleWindow(modules)));

        // create expanded modules window
        ExpandedModulesWindow expanded = new ExpandedModulesWindow(this, COLOR);
        navigationBar.addNavigationButton(new NavigationButton(this, new LocationIdentifier("textures/icons/expanded.png"), () -> toggleWindow(expanded)));

        // create console window
        ConsoleWindow console = new ConsoleWindow(this, COLOR);
        navigationBar.addNavigationButton(new NavigationButton(this, new LocationIdentifier("textures/icons/console.png"), () -> toggleWindow(console)));

        // create hud editor button
        navigationBar.addNavigationButton(new NavigationButton(this, new LocationIdentifier("textures/icons/hud_editor.png"), UTILS::openHUDEditor));

        // add help button and window
        WelcomeWindow welcomeWindow = new WelcomeWindow(this, COLOR);
        NavigationButton helpButton = new NavigationButton(this, new LocationIdentifier("textures/icons/help.png"), () -> toggleWindow(welcomeWindow));
        helpButton.setX(() -> getScreenWidth() - navigationBar.getHeight());
        helpButton.setWidth(navigationBar::getHeight);
        helpButton.setHeight(navigationBar::getHeight);
        navigationBar.add(helpButton);

        // add windows
        windows.addAll(Arrays.asList(modules, console, welcomeWindow));
    }

    @Override
    public void draw(int mouseX, int mouseY, float partialTicks) {
        super.draw(mouseX, mouseY, partialTicks);

        // find window which is hovered or dragging first
        Window hovered = windows.stream().filter(element -> element.isMouseOver(mouseX, mouseY) || element.dragging).findFirst().orElse(null);

        // draw windows
        for(int i = windows.size() - 1; i >= 0; i--) {
            Window window = windows.get(i);
            if(!window.isVisible()) continue;

            // only give accurate mouse info to the window found hovering
            if(window == hovered) window.draw(mouseX, mouseY, partialTicks);
            else window.draw(-1, -1, partialTicks);
        }
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        Window clickedWindow = null;
        for(Window window: windows) {
            if(!window.isVisible()) continue;
            window.click(mouseX, mouseY, mouseButton);
            if(window.isMouseOver(mouseX, mouseY)) {
                clickedWindow = window;
                break;
            }
        }

        if(clickedWindow != null) {
            windows.remove(clickedWindow);
            windows.add(0, clickedWindow);
        }

        super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    public void mouseReleased(int mouseX, int mouseY, int mouseButton) {
        windows.stream().filter(Window::isVisible).forEach(window -> window.release(mouseX, mouseY, mouseButton));
        super.mouseReleased(mouseX, mouseY, mouseButton);
    }

    @Override
    public void keyTyped(Character typedChar, int keyCode) {
        windows.stream().filter(Window::isVisible).forEach(window -> window.keyTyped(typedChar, keyCode));
        super.keyTyped(typedChar, keyCode);
    }

    @Override
    public void mouseScrolled(double mouseX, double mouseY, double value) {
        for(Window window: windows) {
            if(window.isVisible() && window.isMouseOver(mouseX, mouseY)) {
                window.scroll(mouseX, mouseY, value);
                break;
            }
        }
        super.mouseScrolled(mouseX, mouseY, value);
    }

    private void toggleWindow(Window window) {
        window.toggleVisibility();
        windows.remove(window);
        windows.add(0, window);
    }

    /**
     * Stores globals which this GUI uses
     */
    public interface Globals {
        /**
         * Background image of hover background
         */
        LocationIdentifier HOVER_BACKGROUND = new LocationIdentifier("textures/rounded_rectangle.png");

        /**
         * Background color of hover background
         */
        Color HOVER_BACKGROUND_COLOR = new Color(0.5f, 0.5f, 0.5f, 0.2f);
    }
}
