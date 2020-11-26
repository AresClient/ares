package dev.tigr.ares.core.gui.impl.game.window.windows.modules;

import dev.tigr.ares.core.feature.module.Category;
import dev.tigr.ares.core.gui.api.Element;
import dev.tigr.ares.core.gui.api.GUI;
import dev.tigr.ares.core.util.function.DynamicValue;
import dev.tigr.ares.core.util.global.Utils;
import dev.tigr.ares.core.util.render.Color;

import java.util.ArrayList;
import java.util.List;

import static dev.tigr.ares.core.Ares.RENDERER;
import static dev.tigr.ares.core.Ares.RENDER_STACK;

/**
 * @author Tigermouthbear 6/23/20
 */
public class CategoryElement extends Element {
    private final List<ModuleElement> left = new ArrayList<>();
    private final List<ModuleElement> right = new ArrayList<>();

    private double leftOffset = 0;
    private double rightOffset = 0;

    public CategoryElement(GUI gui, Category category, DynamicValue<Color> color) {
        super(gui);

        setVisibility(() -> CategoryButton.getSelectedCategory() == category);

        // create module elements
        ModuleElement prevLeft = null;
        ModuleElement prevRight = null;
        for(int i = 0; i < category.getModules().size(); i++) {
            int finalI = i;
            ModuleElement moduleElement = new ModuleElement(getGUI(), category.getModules().get(i), color);
            moduleElement.setX(() -> (finalI % 2) * getWidth() / 2);

            // set y based on which side its on
            if(i % 2 == 0) {
                if(prevLeft != null) {
                    ModuleElement finalPrevLeft = prevLeft;
                    moduleElement.setY(() -> finalPrevLeft.getY() + finalPrevLeft.getHeight());
                }
                moduleElement.setOffset(() -> leftOffset);
                prevLeft = moduleElement;
                left.add(moduleElement);
            } else {
                if(prevRight != null) {
                    ModuleElement finalPrevRight = prevRight;
                    moduleElement.setY(() -> finalPrevRight.getY() + finalPrevRight.getHeight());
                }
                moduleElement.setOffset(() -> rightOffset);
                prevRight = moduleElement;
                right.add(moduleElement);
            }
            add(moduleElement);
        }
    }

    @Override
    public void draw(int mouseX, int mouseY, float partialTicks) {
        // draw left column
        RENDERER.startScissor(getRenderX(), getRenderY(), getWidth() / 2d, getHeight());
        RENDER_STACK.translate(0, leftOffset, 0);
        left.stream().filter(Element::isVisible).forEach(element -> element.draw(mouseX, mouseY, partialTicks));
        RENDER_STACK.translate(0, -leftOffset, 0);
        RENDERER.stopScissor();

        // draw right column
        RENDERER.startScissor(getRenderX() + getWidth() / 2d, getRenderY(), getWidth() / 2d, getHeight());
        RENDER_STACK.translate(0, rightOffset, 0);
        right.stream().filter(Element::isVisible).forEach(element -> element.draw(mouseX, mouseY, partialTicks));
        RENDER_STACK.translate(0, -rightOffset, 0);
        RENDERER.stopScissor();
    }

    @Override
    public void scroll(double mouseX, double mouseY, double value) {
        super.scroll(mouseX, mouseY, value);

        if(value != 0) {
            value /= 10;
            if(mouseX < getRenderX() + getWidth() / 2) leftOffset = getScrollValue(left, value, leftOffset);
            else rightOffset = getScrollValue(right, value, rightOffset);
        }
    }

    public double getScrollValue(List<ModuleElement> elements, double value, double curr) {
        double height = getHeight(elements);
        if(height > getHeight()) {
            double range = height - getHeight();
            value = curr + value;
            value = Utils.clamp(value, -range, 0);
            return value;
        } else return 0;
    }

    public double getHeight(List<ModuleElement> elements) {
        double height = 0;
        for(ModuleElement element: elements) {
            height += element.getHeight();
        }
        return height;
    }
}
