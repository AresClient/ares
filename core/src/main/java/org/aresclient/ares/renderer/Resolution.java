package org.aresclient.ares.renderer;

public class Resolution {
    private final int width, height, scaledWidth, scaledHeight;
    private final double scaleFactor;

    public Resolution(int width, int height, int scaledWidth, int scaledHeight, double scaleFactor) {
        this.width = width;
        this.height = height;
        this.scaledWidth = scaledWidth;
        this.scaledHeight = scaledHeight;
        this.scaleFactor = scaleFactor;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public int getScaledWidth() {
        return scaledWidth;
    }

    public int getScaledHeight() {
        return scaledHeight;
    }

    public double getScaleFactor() {
        return scaleFactor;
    }
}
