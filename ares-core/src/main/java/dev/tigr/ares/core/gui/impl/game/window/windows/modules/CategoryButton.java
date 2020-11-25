package dev.tigr.ares.core.gui.impl.game.window.windows.modules;

import dev.tigr.ares.core.feature.module.Category;
import dev.tigr.ares.core.gui.api.Element;
import dev.tigr.ares.core.gui.api.GUI;
import dev.tigr.ares.core.gui.impl.game.ClickGUI;
import dev.tigr.ares.core.util.render.Color;
import dev.tigr.ares.core.util.render.LocationIdentifier;

import static dev.tigr.ares.core.Ares.FONT_RENDERER;
import static dev.tigr.ares.core.Ares.RENDERER;

/**
 * @author Tigermouthbear 6/23/20
 */
public class CategoryButton extends Element implements ClickGUI.Globals {
    private static Category selectedCategory = Category.COMBAT;

    private final Category category;
    private final LocationIdentifier resourceLocation;

    public CategoryButton(GUI gui, Category category) {
        super(gui);

        this.category = category;
        this.resourceLocation = new LocationIdentifier("textures/categories/" + category.name().toLowerCase() + ".png");

        setX(() -> 1d);
        setWidth(() -> getParent().getWidth() / 4 - 2);
        setHeight(() -> getParent().getWidth() / 12);
    }

    public static Category getSelectedCategory() {
        return selectedCategory;
    }

    @Override
    public void draw(int mouseX, int mouseY, float partialTicks) {
        super.draw(mouseX, mouseY, partialTicks);

        // draw background for hovered or selected buttons
        if(category == selectedCategory || isMouseOver(mouseX, mouseY))
            RENDERER.drawRect(getRenderX(), getRenderY(), getWidth(), getHeight(), HOVER_BACKGROUND_COLOR);

        // draw icon
        RENDERER.drawImage(getRenderX(), getRenderY(), getHeight(), getHeight(), resourceLocation);

        // draw category name
        FONT_RENDERER.drawStringWithCustomHeight(category.name(), getRenderX() + getHeight(), getRenderY() + getHeight() / 2d - (getHeight() / 3d) / 2d, Color.WHITE, getHeight() / 3d);
    }

    @Override
    public void click(int mouseX, int mouseY, int mouseButton) {
        if(isMouseOver(mouseX, mouseY) && mouseButton == 0) selectedCategory = category;
        super.click(mouseX, mouseY, mouseButton);
    }
}
