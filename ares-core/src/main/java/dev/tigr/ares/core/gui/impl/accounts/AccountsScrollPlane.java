package dev.tigr.ares.core.gui.impl.accounts;


import dev.tigr.ares.core.gui.api.Element;
import dev.tigr.ares.core.gui.api.GUI;
import dev.tigr.ares.core.util.global.Utils;

import static dev.tigr.ares.core.Ares.RENDERER;
import static dev.tigr.ares.core.Ares.RENDER_STACK;

/**
 * Holds all accounts elements and allows scrolling
 *
 * @author Tigermouthbear 7/15/20
 */
public class AccountsScrollPlane extends Element {
    private double scrollOffset = 0;

    public AccountsScrollPlane(GUI gui) {
        super(gui);
    }

    @Override
    public void draw(int mouseX, int mouseY, float partialTicks) {
        // scissor area
        RENDERER.startScissor(getRenderX(), getRenderY(), getWidth(), getHeight());

        // transform for scroll
        RENDER_STACK.translate(0, scrollOffset, 0);

        super.draw(mouseX, mouseY, partialTicks);

        // undo scroll transform
        RENDER_STACK.translate(0, -scrollOffset, 0);

        // pop attrib
        RENDERER.stopScissor();
    }

    @Override
    public void scroll(double mouseX, double mouseY, double value) {
        super.scroll(mouseX, mouseY, value);

        if(value != 0) {
            value /= 10;
            scrollOffset = calculateScroll(scrollOffset, value);
        }
    }

    public double calculateScroll(double prev, double value) {
        double max = 0;
        for(Element element: getChildren()) {
            double curr = element.getY() + element.getHeight();
            if(curr > max) max = curr;
        }

        if(max > getHeight()) {
            double range = max - getHeight();
            value = prev + value;
            value = Utils.clamp(value, -range, 0);
            return value;
        } else return 0;
    }
}
