package dev.tigr.ares.core.gui.api;

import dev.tigr.ares.core.feature.module.ClickGUIMod;
import dev.tigr.ares.core.util.global.Utils;

import java.util.ArrayList;
import java.util.List;

import static dev.tigr.ares.core.Ares.*;

/**
 * Custom Implementation of a GUI screen
 *
 * @author Tigermouthbear 6/16/20
 * updated 11/20/20 to make more generic
 */
public class GUI {
    /**
     * List of elements on the GUI
     */
    private final List<Element> elements = new ArrayList<>();

    /**
     * Stores the current tooltip text, blank for not rendered
     */
    private String tooltip = "";

    /**
     * Calls the draw method when the screen is drawn
     *
     * @param mouseX       mouse's X position on the screen
     * @param mouseY       mouse's Y position on the screen
     * @param partialTicks partial ticks for rendering
     */
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        RENDER_STACK.push();

        // draw all elements
        draw(mouseX, mouseY, partialTicks);

        // render tooltip clamped onto screen
        if(!tooltip.equals("")) {
            RENDERER.drawTooltip(tooltip, (int) Utils.clamp(mouseX, 0, getScreenWidth() - (FONT_RENDERER.getStringWidth(tooltip) + 4)) - 2, mouseY, ClickGUIMod.getColor());
            tooltip = "";
        }

        RENDER_STACK.pop();
    }

    /**
     * Calls the draw method for all visible elements when the mouse is clicked
     *
     * @param mouseX       mouse's X position on the screen
     * @param mouseY       mouse's Y position on the screen
     * @param partialTicks partial ticks for rendering
     */
    public void draw(int mouseX, int mouseY, float partialTicks) {
        // render all elements
        elements.stream().filter(Element::isVisible).forEach(element -> element.draw(mouseX, mouseY, partialTicks));
    }

    /**
     * Calls the click method for all visible elements when the mouse is clicked
     *
     * @param mouseX      mouse's X position on the screen
     * @param mouseY      mouse's Y position on the screen
     * @param mouseButton mouse button clicked
     */
    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        elements.stream().filter(Element::isVisible).forEach(element -> element.click(mouseX, mouseY, mouseButton));
    }

    /**
     * Calls the released method for all visible elements when the mouse is released
     *
     * @param mouseX      mouse's X position on the screen
     * @param mouseY      mouse's Y position on the screen
     * @param mouseButton mouse button released
     */
    public void mouseReleased(int mouseX, int mouseY, int mouseButton) {
        elements.stream().filter(Element::isVisible).forEach(element -> element.release(mouseX, mouseY, mouseButton));
    }

    /**
     * Calls the keyTyped method for all visible elements when a key is typed
     *  @param typedChar {@link Character} typed
     * @param keyCode   Keycode of character typed
     */
    public void keyTyped(Character typedChar, int keyCode) {
        elements.stream().filter(Element::isVisible).forEach(element -> element.keyTyped(typedChar, keyCode));
    }

    /**
     * Calls the scroll method for all visible elements when the mouse wheel is scrolled
     *
     * @param mouseX x position of the scroll
     * @param mouseY y position of the scroll
     * @param value  strength of the scroll
     * @return mc default return val
     */
    public void mouseScrolled(double mouseX, double mouseY, double value) {
        elements.stream().filter(Element::isVisible).forEach(element -> element.scroll(mouseX, mouseY, value));
    }

    /**
     * Calls the close method for all elements
     */
    public void onGuiClosed() {
        elements.forEach(Element::close);
    }

    /**
     * Adds an element to the GUI
     *
     * @param element element to add
     * @param <T>     type of element
     * @return element passed in
     */
    public <T extends Element> T add(T element) {
        elements.add(element);
        return element;
    }

    /**
     * Getter for list of all elements on GUI
     *
     * @return list of all elements on GUI
     */
    public List<Element> getElements() {
        return elements;
    }

    /**
     * Getter for GUI's screen width
     *
     * @return screen width
     */
    public int getScreenWidth() {
        return GUI_MANAGER.getWidth();
    }

    /**
     * Getter for GUI's screen height
     *
     * @return screen height
     */
    public int getScreenHeight() {
        return GUI_MANAGER.getHeight();
    }

    /**
     * Getter for GUI's zLevel
     *
     * @return zLevel
     */
    public float getZLevel() {
        return GUI_MANAGER.getZLevel();
    }

    /**
     * Setter for tooltip text
     *
     * @param value text
     */
    public void setTooltip(String value) {
        tooltip = value;
    }
}
