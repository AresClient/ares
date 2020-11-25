package dev.tigr.ares.core.gui.impl.game.nav;

import dev.tigr.ares.core.gui.api.Element;
import dev.tigr.ares.core.gui.api.GUI;
import dev.tigr.ares.core.gui.impl.game.ClickGUI;
import dev.tigr.ares.core.util.function.DynamicValue;
import dev.tigr.ares.core.util.function.Hook;
import dev.tigr.ares.core.util.render.Color;
import dev.tigr.ares.core.util.render.LocationIdentifier;

import static dev.tigr.ares.core.Ares.RENDERER;

/**
 * @author Tigermouthbear 6/16/20
 */
public class NavigationButton extends Element implements ClickGUI.Globals {
    private final LocationIdentifier icon;
    private final Hook onClick;
    private final DynamicValue<Color> color;

    public NavigationButton(GUI gui, LocationIdentifier icon, Hook onClick) {
        this(gui, icon, onClick, () -> Color.WHITE);
    }

    public NavigationButton(GUI gui, LocationIdentifier icon, Hook onClick, DynamicValue<Color> color) {
        super(gui);
        this.icon = icon;
        this.onClick = onClick;
        this.color = color;
    }

    @Override
    public void draw(int mouseX, int mouseY, float partialTicks) {
        // draw rounded background on hover
        if(isMouseOver(mouseX, mouseY))
            RENDERER.drawImage(getRenderX(), getRenderY(), getWidth(), getHeight(), HOVER_BACKGROUND, HOVER_BACKGROUND_COLOR);

        // draw icon
        RENDERER.drawImage(getRenderX(), getRenderY(), getWidth(), getHeight(), icon, color.getValue());

        // draw child elements
        super.draw(mouseX, mouseY, partialTicks);
    }

    @Override
    public void click(int mouseX, int mouseY, int mouseButton) {
        if(isMouseOver(mouseX, mouseY) && mouseButton == 0) onClick.invoke();
        super.click(mouseX, mouseY, mouseButton); // call child element's click method
    }
}
