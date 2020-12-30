package dev.tigr.ares.core.gui.impl.game.window.windows.modules;

import dev.tigr.ares.core.feature.module.Category;
import dev.tigr.ares.core.gui.api.Element;
import dev.tigr.ares.core.gui.api.GUI;
import dev.tigr.ares.core.gui.impl.game.window.Window;
import dev.tigr.ares.core.util.function.DynamicValue;
import dev.tigr.ares.core.util.render.Color;

import static dev.tigr.ares.core.Ares.RENDER_STACK;

/**
 * @author Tigermouthbear 12/30/20
 */
public class ExpandedModulesWindow extends Window {
    public ExpandedModulesWindow(GUI gui, DynamicValue<Color> color) {
        super(gui, "Modules (EXPANDED)", color, false, 0, 0);

        // remove x button from window
        getChildren().clear();

        double y = 0;
        for(Category category: Category.values()) {
            add(new ExpandedCategoryElement(gui, color, category, settingCategory, 0.04, 0.1 + y));
            y += 0.04;
        }
    }

    @Override
    public void draw(int mouseX, int mouseY, float partialTicks) {
        openCloseTimer.tick();

        // scale for animation
        double animScale = Math.max(openCloseTimer.getAnimationFactor(), 0.01);
        RENDER_STACK.scale(animScale, animScale, 1);

        // draw child element's
        getChildren().stream().filter(Element::isVisible).forEach(element -> element.draw(mouseX, mouseY, partialTicks));

        // reset animation scale
        RENDER_STACK.scale(1 / animScale, 1 / animScale, 1);
    }

    @Override
    public boolean isMouseOver(double mouseX, double mouseY) {
        return getChildren().stream().anyMatch(element -> element.isMouseOver(mouseX, mouseY));
    }
}
