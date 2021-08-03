package dev.tigr.ares.core.gui.impl.game.window.windows.modules;

import dev.tigr.ares.core.feature.module.Category;
import dev.tigr.ares.core.gui.api.GUI;
import dev.tigr.ares.core.gui.impl.game.window.Window;
import dev.tigr.ares.core.util.function.DynamicValue;
import dev.tigr.ares.core.util.render.Color;

import java.util.ArrayList;
import java.util.List;

import static dev.tigr.ares.core.Ares.RENDER_STACK;

/**
 * @author Tigermouthbear 12/30/20
 */
public class ExpandedModulesWindow extends Window {
    private final List<ExpandedCategoryElement> windows = new ArrayList<>();

    public ExpandedModulesWindow(GUI gui, DynamicValue<Color> color) {
        super(gui, "Modules (EXPANDED)", color, false, 0, 0);

        // remove x button from window
        getChildren().clear();

        double y = 0;
        for(Category category: Category.values()) {
            windows.add(new ExpandedCategoryElement(gui, color, category, settingCategory, 0.04, 0.1 + y));
            y += 0.04;
        }
    }

    @Override
    public void draw(int mouseX, int mouseY, float partialTicks) {
        openCloseTimer.tick();

        // scale for animation
        double animScale = Math.max(openCloseTimer.getAnimationFactor(), 0.01);
        RENDER_STACK.scale(animScale, animScale, 1);

        // find window which is hovered or dragging first
        ExpandedCategoryElement hovered = windows.stream().filter(element -> element.isMouseOver(mouseX, mouseY) || element.dragging).findFirst().orElse(null);

        // draw windows
        for(int i = windows.size() - 1; i >= 0; i--) {
            ExpandedCategoryElement window = windows.get(i);
            if(!window.isVisible()) continue;

            // only give accurate mouse info to the window found hovering
            if(window == hovered) window.draw(mouseX, mouseY, partialTicks);
            else window.draw(-1, -1, partialTicks);
        }

        // reset animation scale
        RENDER_STACK.scale(1 / animScale, 1 / animScale, 1);
    }

    @Override
    public void click(int mouseX, int mouseY, int mouseButton) {
        ExpandedCategoryElement clickedWindow = null;
        for(ExpandedCategoryElement window: windows) {
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

        super.click(mouseX, mouseY, mouseButton);
    }

    @Override
    public void release(int mouseX, int mouseY, int mouseButton) {
        for(ExpandedCategoryElement window: windows) {
            if(window.isVisible()) {
                window.release(mouseX, mouseY, mouseButton);
            }
        }
        super.release(mouseX, mouseY, mouseButton);
    }

    @Override
    public void keyTyped(Character typedChar, int keyCode) {
        super.keyTyped(typedChar, keyCode);
        for(ExpandedCategoryElement window: windows) {
            if(window.isVisible()) {
                window.keyTyped(typedChar, keyCode);
            }
        }
    }

    @Override
    public void scroll(double mouseX, double mouseY, double value) {
        for(ExpandedCategoryElement window: windows) {
            if(window.isVisible() && window.isMouseOver(mouseX, mouseY)) {
                window.scroll(mouseX, mouseY, value);
                break;
            }
        }
        super.scroll(mouseX, mouseY, value);
    }

    @Override
    public boolean isMouseOver(double mouseX, double mouseY) {
        for(ExpandedCategoryElement element: windows) {
            if(element.isMouseOver(mouseX, mouseY) || element.dragging) {
                return true;
            }
        }
        return false;
    }
}
