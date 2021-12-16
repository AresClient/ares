package dev.tigr.ares.core.util.render;

import dev.tigr.ares.core.util.math.AABox;

/**
 * @author Tigermouthbear
 * provides abstraction for rendering layer
 */
public interface IRenderer {
    /**
     * Returns the current color of the cycling rainbow
     *
     * @return color
     */
    static Color rainbow() {
        float hue = (System.currentTimeMillis() % (320 * 32)) / (320f * 32);
        return new Color(Color.HSBtoRGB(hue, 1, 1));
    }

    void drawImage(double x, double y, double width, double height, LocationIdentifier identifier);

    void drawImage(double x, double y, double width, double height, LocationIdentifier identifier, Color color);

    void drawLine(double x1, double y1, double x2, double y2, int weight, Color color);

    void drawLine(double x1, double y1, double z1, double x2, double y2, double z2, int weight, Color color);

    void drawLineLoop(int weight, Color color, double... points);

    void drawRect(double x, double y, double width, double height, Color color);

    void startScissor(double x, double y, double width, double height);

    void stopScissor();

    void drawTooltip(String text, int mouseX, int mouseY, Color color);

    void prepare3d();

    void end3d();

    void drawBox(AABox box, Color fillColor, Color lineColor, int... ignoredSides);
}
