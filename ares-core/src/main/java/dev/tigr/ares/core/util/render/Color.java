package dev.tigr.ares.core.util.render;

/**
 * @author Tigermouthbear
 */
public class Color {
    public static final Color BLACK = new Color(0, 0, 0, 0.8f);
    public static final Color ARES_RED = new Color(0.54f, 0.03f, 0.03f, 1);
    public static final Color GRAY = new Color(0.5f, 0.5f, 0.5f, 1);
    public static final Color WHITE = new Color(1, 1, 1, 1);
    public static final Color RED = new Color(1, 0, 0, 1);
    public static final Color GREEN = new Color(0, 1, 0, 1);
    public static final Color COLORLESS = new Color(0,0,0,0);

    private float r;
    private float g;
    private float b;
    private float a;

    public Color(int rgb) {
        this((float) (rgb >> 16) / 255.0F, (float) (rgb >> 8 & 255) / 255.0F, (float) (rgb & 255) / 255.0F, 1);
    }

    public Color(float r, float g, float b, float a) {
        this.r = r;
        this.g = g;
        this.b = b;
        this.a = a;
    }

    public static Color fromDistance(float distance) {
        float fraction = distance > 50 ? 1 : distance / 50;
        return fraction > 0.5d ? new Color(1 - (fraction - 0.5f) * 2, 1, 0, 1) : new Color(1, fraction * 2, 0, 1);
    }

    public static int HSBtoRGB(float hue, float saturation, float brightness) {
        if(saturation == 0)
            return convert(brightness, brightness, brightness, 0);
        if(saturation < 0 || saturation > 1 || brightness < 0 || brightness > 1)
            throw new IllegalArgumentException();
        hue = hue - (float) Math.floor(hue);
        int i = (int) (6 * hue);
        float f = 6 * hue - i;
        float p = brightness * (1 - saturation);
        float q = brightness * (1 - saturation * f);
        float t = brightness * (1 - saturation * (1 - f));
        switch(i) {
            case 0:
                return convert(brightness, t, p, 0);
            case 1:
                return convert(q, brightness, p, 0);
            case 2:
                return convert(p, brightness, t, 0);
            case 3:
                return convert(p, q, brightness, 0);
            case 4:
                return convert(t, p, brightness, 0);
            case 5:
                return convert(brightness, p, q, 0);
            default:
                throw new InternalError("impossible");
        }
    }

    private static int convert(float red, float green, float blue, float alpha) {
        if(red < 0 || red > 1 || green < 0 || green > 1 || blue < 0 || blue > 1
                || alpha < 0 || alpha > 1)
            throw new IllegalArgumentException("Bad RGB values");
        int redval = Math.round(255 * red);
        int greenval = Math.round(255 * green);
        int blueval = Math.round(255 * blue);
        int alphaval = Math.round(255 * alpha);
        return (alphaval << 24) | (redval << 16) | (greenval << 8) | blueval;
    }

    public float getRed() {
        return r;
    }

    public float getGreen() {
        return g;
    }

    public float getBlue() {
        return b;
    }

    public float getAlpha() {
        return a;
    }

    public Color setR(float value) {
        r = value;
        return this;
    }

    public Color setG(float value) {
        g = value;
        return this;
    }

    public Color setB(float value) {
        b = value;
        return this;
    }

    //
    //  java.awt.Color
    //

    public Color setA(float value) {
        a = value;
        return this;
    }

    public int getRGB() {
        return (((int) (getAlpha() * 255 + 0.5) & 0xFF) << 24) |
                (((int) (getRed() * 255 + 0.5) & 0xFF) << 16) |
                (((int) (getGreen() * 255 + 0.5) & 0xFF) << 8) |
                (((int) (getBlue() * 255 + 0.5) & 0xFF));
    }

    public Color asTransparent() {
        return new Color(r,g,b,0);
    }

    public Color getColorBetween(Color color) {
        return getColorBetween(new Color(r, g, b, a), color);
    }

    public static Color rainbow() {
        float hue = (System.currentTimeMillis() % (320 * 32)) / (320f * 32);
        return new Color(Color.HSBtoRGB(hue, 1, 1));
    }

    public static Color rainbow(int speed, float offset, float saturation, float brightness) {
        float hue = ((System.currentTimeMillis() % ((speed *10) *speed)) / ((speed *10f) *speed)) -offset;
        float hue2 = hue;
        if(hue<0) hue2 = 1f + hue;
        return new Color(Color.HSBtoRGB(hue2, saturation, brightness));
    }

    public static Color getTransparent(Color color) {
        return color.asTransparent();
    }

    public static Color getTransparent(boolean colorless, Color color) {
        if(colorless) return COLORLESS;
        else return color.asTransparent();
    }

    public static Color getColorBetween(Color color1, Color color2) {
        float
                r = (color1.getRed() + color2.getRed()) /2,
                g = (color1.getGreen() + color2.getGreen()) /2,
                b = (color1.getBlue() + color2.getBlue()) /2,
                a = (color1.getAlpha() + color2.getAlpha()) /2;
        return new Color(r,g,b,a);
    }
}
