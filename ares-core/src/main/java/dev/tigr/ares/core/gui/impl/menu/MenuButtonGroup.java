package dev.tigr.ares.core.gui.impl.menu;

import dev.tigr.ares.core.gui.api.Element;
import dev.tigr.ares.core.gui.api.GUI;

/**
 * @author Makrennel 09/28/21
 * updated 11/30/21 by Tigermouthbear
 */
public class MenuButtonGroup extends Element {
    private Element last = null;
    private final double spacing = 5;

    public MenuButtonGroup(GUI parent) {
        super(parent);
    }

    @Override
    public <T extends Element> T add(T element) {
        element.setX(() -> getWidth() / 2D - element.getWidth() / 2D);
        if(last != null) {
            final Element finalElement = last;
            element.setY(() -> finalElement.getY() + finalElement.getHeight() + spacing);
        }
        last = element;

        element.setWidth(this::getWidth);
        setHeight(() -> element.getY() + element.getHeight());

        return super.add(element);
    }
}
