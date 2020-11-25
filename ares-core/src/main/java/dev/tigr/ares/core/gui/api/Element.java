package dev.tigr.ares.core.gui.api;

import dev.tigr.ares.core.util.function.DynamicValue;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents an element on an {@link GUI}
 *
 * @author Tigermouthbear 6/16/20
 */
public class Element {
    /**
     * List of element's child elements
     */
    private final List<Element> children = new ArrayList<>();
    /**
     * {@link DynamicValue} of element's X position, defaults to 0
     */
    private DynamicValue<Double> x = () -> 0d;
    /**
     * {@link DynamicValue} of element's Y position, defaults to 0
     */
    private DynamicValue<Double> y = () -> 0d;
    /**
     * {@link DynamicValue} of element's width, defaults to 0
     */
    private DynamicValue<Double> width = () -> 0d;
    /**
     * {@link DynamicValue} of element's height, defaults to 0
     */
    private DynamicValue<Double> height = () -> 0d;
    /**
     * {@link DynamicValue} of element's visibility, defaults to true
     */
    private DynamicValue<Boolean> visibility = () -> true;
    /**
     * Element's parent element, null for no parent
     */
    private Element parent = null;

    /**
     * The {@link GUI} that the element is on
     */
    private GUI gui;

    public Element(GUI gui) {
        this.gui = gui;
    }

    /**
     * Draw listener which is called on parent GUI screen's draw method
     *
     * @param mouseX       mouse's X position on the screen
     * @param mouseY       mouse's Y position on the screen
     * @param partialTicks partial ticks for rendering
     */
    public void draw(int mouseX, int mouseY, float partialTicks) {
        children.stream().filter(Element::isVisible).forEach(element -> element.draw(mouseX, mouseY, partialTicks));
    }

    /**
     * Click listener which is called on the parent GUI screen's click listener
     *
     * @param mouseX      mouse's X position on the screen
     * @param mouseY      mouse's Y position on the screen
     * @param mouseButton mouse button clicked
     */
    public void click(int mouseX, int mouseY, int mouseButton) {
        children.stream().filter(Element::isVisible).forEach(element -> element.click(mouseX, mouseY, mouseButton));
    }

    /**
     * Mouse release listener which is called on the parent GUI screen's mouse release listener
     *
     * @param mouseX      mouse's X position on the screen
     * @param mouseY      mouse's Y position on the screen
     * @param mouseButton mouse button released
     */
    public void release(int mouseX, int mouseY, int mouseButton) {
        children.stream().filter(Element::isVisible).forEach(element -> element.release(mouseX, mouseY, mouseButton));
    }

    /**
     * Key press listener which is called on the parent GUI screen's key typed listener
     *
     * @param typedChar {@link Character} typed
     * @param keyCode   Keycode of character typed
     */
    public void keyTyped(Character typedChar, int keyCode) {
        children.stream().filter(Element::isVisible).forEach(element -> element.keyTyped(typedChar, keyCode));
    }

    /**
     * Scroll listener which is called when the mouse is scrolled
     *
     * @param mouseX x position of the scroll
     * @param mouseY y position of the scroll
     * @param value  strength of the scroll
     */
    public void scroll(double mouseX, double mouseY, double value) {
        children.stream().filter(Element::isVisible).forEach(element -> element.scroll(mouseX, mouseY, value));
    }

    /**
     * Method called on GUI close
     */
    public void close() {
        children.forEach(Element::close);
    }

    /**
     * Adds element to this element's child list
     *
     * @param element element to add to children list
     * @param <T>     type of element
     * @return element which was passed in
     */
    public <T extends Element> T add(T element) {
        children.add(element);
        element.setParent(this);
        element.setGUI(getGUI());
        return element;
    }

    /**
     * Getter for elements actual X position on the screen
     *
     * @return actual X position on screen
     */
    public double getRenderX() {
        return getParent() == null ? getX() : getX() + getParent().getRenderX();
    }

    /**
     * Getter for elements actual Y position on the screen
     *
     * @return actual Y position on screen
     */
    public double getRenderY() {
        return getParent() == null ? getY() : getY() + getParent().getRenderY();
    }

    /**
     * Gets if mouse is over element at position
     *
     * @param mouseX mouse's X position on the screen
     * @param mouseY mouse's Y position on the screen
     * @return true if mouse is over element
     */
    public boolean isMouseOver(double mouseX, double mouseY) {
        return mouseX >= getRenderX()
                && mouseX <= getRenderX() + getWidth()
                && mouseY >= getRenderY()
                && mouseY <= getRenderY() + getHeight();
    }

    /**
     * Getter for element's dynamic X value
     *
     * @return elements X position
     */
    public double getX() {
        return x.getValue();
    }

    /**
     * Setter for element's dynamic X value
     *
     * @param value {@link DynamicValue}
     */
    public void setX(DynamicValue<Double> value) {
        x = value;
    }

    /**
     * Getter for element's dynamic Y value
     *
     * @return elements Y position
     */
    public double getY() {
        return y.getValue();
    }

    /**
     * Setter for element's dynamic Y value
     *
     * @param value {@link DynamicValue}
     */
    public void setY(DynamicValue<Double> value) {
        y = value;
    }

    /**
     * Getter for element's dynamic width value
     *
     * @return elements width
     */
    public double getWidth() {
        return width.getValue();
    }

    /**
     * Setter for element's dynamic width value
     *
     * @param value {@link DynamicValue}
     */
    public void setWidth(DynamicValue<Double> value) {
        width = value;
    }

    /**
     * Getter for element's dynamic height value
     *
     * @return elements height
     */
    public double getHeight() {
        return height.getValue();
    }

    /**
     * Setter for element's dynamic height value
     *
     * @param value {@link DynamicValue}
     */
    public void setHeight(DynamicValue<Double> value) {
        height = value;
    }

    /**
     * Getter for element's dynamic visibility value
     *
     * @return elements visibility
     */
    public boolean isVisible() {
        return visibility.getValue();
    }

    /**
     * Setter for element's dynamic visibility value
     *
     * @param value {@link DynamicValue}
     */
    public void setVisibility(DynamicValue<Boolean> value) {
        visibility = value;
    }

    /**
     * Toggles the visibility to the opposite of what it currently is
     */
    public void toggleVisibility() {
        boolean visible = visibility.getValue();
        visibility = () -> !visible;
    }

    /**
     * Getter for element's child list
     *
     * @return children of element
     */
    public List<Element> getChildren() {
        return children;
    }

    /**
     * Getter for element's parent
     *
     * @return parent of element
     */
    public Element getParent() {
        return parent;
    }

    /**
     * Setter for element's parent element
     *
     * @param value parent element
     */
    public void setParent(Element value) {
        parent = value;
    }

    /**
     * Getter for element's GUI
     *
     * @return GUI of element
     */
    public GUI getGUI() {
        return gui;
    }

    /**
     * Setter for the GUI that the element is on
     *
     * @param value GUI which the element is on
     */
    public void setGUI(GUI value) {
        this.gui = value;
    }

    /**
     * Getter for parent GUI's screen width
     *
     * @return screen width
     */
    public double getScreenWidth() {
        return getGUI().getScreenWidth();
    }

    /**
     * Getter for parent GUI's screen height
     *
     * @return screen height
     */
    public double getScreenHeight() {
        return getGUI().getScreenHeight();
    }

    /**
     * Setter for the tooltip of the parent GUI
     *
     * @param value tooltip text
     */
    public void setTooltip(String value) {
        getGUI().setTooltip(value);
    }
}
