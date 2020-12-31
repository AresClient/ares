package dev.tigr.ares.core.gui.impl.game.window.windows.modules;

import dev.tigr.ares.core.feature.module.Category;
import dev.tigr.ares.core.gui.api.GUI;
import dev.tigr.ares.core.gui.impl.game.window.Window;
import dev.tigr.ares.core.util.function.DynamicValue;
import dev.tigr.ares.core.util.render.Color;

import static dev.tigr.ares.core.Ares.*;

/**
 * @author Tigermouthbear 6/23/20
 */
public class CompactModulesWindow extends Window {
    public CompactModulesWindow(GUI gui, DynamicValue<Color> color) {
        super(gui, "Modules", color, false, 0.03, 0.1);

        setWidth(() -> getScreenWidth() / 3d);
        setHeight(() -> getScreenHeight() / 2d);

        // add category buttons
        CategoryButton prev = null;
        for(Category category: Category.values()) {
            // create category button
            CategoryButton button = new CategoryButton(getGUI(), category);
            if(prev == null) button.setY(this::getBarHeight);
            else {
                CategoryButton finalPrev = prev;
                button.setY(() -> finalPrev.getY() + finalPrev.getHeight());
            }

            // add button to window
            add(button);
            prev = button;

            // add category element to window
            CategoryElement categoryElement = new CategoryElement(getGUI(), category, color, () -> getHeight() / 12d, 2);
            categoryElement.setX(() -> getWidth() / 4 + 1);
            categoryElement.setY(this::getBarHeight);
            categoryElement.setWidth(() -> getWidth() / 4 * 3);
            categoryElement.setHeight(() -> getHeight() - getBarHeight());
            categoryElement.setVisibility(() -> CategoryButton.getSelectedCategory() == category);
            add(categoryElement);
        }
    }

    @Override
    public void draw(int mouseX, int mouseY) {
        // draw line separating categories
        RENDERER.drawLine((int)(getRenderX() + getWidth() / 4d), getRenderY() + getBarHeight(), (int)(getRenderX() + getWidth() / 4d), getRenderY() + getHeight(), 1, color.getValue());

        // draw line separating modules
        RENDERER.drawLine(getRenderX() + (getWidth() / 4d + 1) + (getWidth() / 4d * 3 / 2), getRenderY() + getBarHeight(), getRenderX() + (getWidth() / 4d + 1) + (getWidth() / 4d * 3d / 2d), getRenderY() + getHeight(), 1, color.getValue());
    }
}
