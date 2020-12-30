package dev.tigr.ares.core.gui.impl.game.window.windows.modules;

import dev.tigr.ares.core.feature.module.Category;
import dev.tigr.ares.core.gui.api.Element;
import dev.tigr.ares.core.gui.api.GUI;
import dev.tigr.ares.core.util.function.DynamicValue;
import dev.tigr.ares.core.util.global.Utils;
import dev.tigr.ares.core.util.render.Color;

import java.util.ArrayList;
import java.util.List;

import static dev.tigr.ares.core.Ares.*;

/**
 * @author Tigermouthbear 6/23/20
 */
public class CategoryElement extends Element {
    private final List<List<ModuleElement>> columnsList = new ArrayList<>();
    private final int columns;
    private final double[] offsets;

    // height of the area to scissor during scroll calculation, used for collapse animation in ExpandedCategoryElement
    private DynamicValue<Double> scissorHeight;

    // no scissor height specified
    public CategoryElement(GUI gui, Category category, DynamicValue<Color> color, DynamicValue<Double> modHeight, int columns) {
        this(gui, category, color, modHeight, () -> 0d, columns);
        scissorHeight = this::getHeight;
    }

    public CategoryElement(GUI gui, Category category, DynamicValue<Color> color, DynamicValue<Double> modHeight, DynamicValue<Double> scissorHeight, int columns) {
        super(gui);

        this.scissorHeight = scissorHeight;

        // setup columns
        this.columns = columns;
        this.offsets = new double[columns];
        for(int i = 0; i < columns; i++) {
            offsets[i] = 0;
            columnsList.add(i, new ArrayList<>());
        }

        setVisibility(() -> CategoryButton.getSelectedCategory() == category);

        // create module elements
        ModuleElement[] prev = new ModuleElement[columns];
        for(int i = 0; i < columns; i++) prev[i] = null;
        for(int i = 0; i < category.getModules().size(); i++) {
            int column = (i + columns) % columns;
            ModuleElement moduleElement = new ModuleElement(getGUI(), category.getModules().get(i), color, modHeight);
            moduleElement.setX(() -> column * getWidth() / columns);
            moduleElement.setWidth(() -> getWidth() / (double) columns);
            
            // set y base on column
            if(prev[column] != null) {
                ModuleElement finalPrev = prev[column];
                moduleElement.setY(() -> finalPrev.getY() + finalPrev.getHeight());
            }
            prev[column] = moduleElement;
            columnsList.get(column).add(moduleElement);

            moduleElement.setOffset(() -> offsets[column]);

            add(moduleElement);
        }
    }

    @Override
    public void draw(int mouseX, int mouseY, float partialTicks) {
        double pos = 0;
        double width = getWidth() / (double) columns;
        for(int i = 0; i < columns; i++) {
            RENDERER.startScissor(getRenderX() + pos, getRenderY(), width, scissorHeight.getValue());
            RENDER_STACK.translate(0, offsets[i], 0);
            columnsList.get(i).stream().filter(Element::isVisible).forEach(element -> element.draw(mouseX, mouseY, partialTicks));
            RENDER_STACK.translate(0, -offsets[i], 0);
            RENDERER.stopScissor();
            pos += width;
        }
    }

    @Override
    public void scroll(double mouseX, double mouseY, double value) {
        super.scroll(mouseX, mouseY, value);

        if(value != 0 && mouseX > getRenderX() && mouseX < getRenderX() + getWidth()) {
            value /= 10;

            // find column mouse is over and scroll
            int col = (int) ((mouseX - getRenderX()) / (getWidth() / columns));

            offsets[col] = getScrollValue(columnsList.get(col), value, offsets[col]);
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
