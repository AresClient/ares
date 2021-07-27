package dev.tigr.ares.core.util.render.font;

import dev.tigr.ares.core.util.render.Color;

import java.util.HashMap;
import java.util.Map;

import static dev.tigr.ares.CoreWrapper.RENDER_STACK;

/**
 * @author Tigermouthbear 7/27/21
 */
public abstract class AbstractFontRenderer {
    private static final Map<GlyphFont, Map<Integer, AbstractGlyphPage>> glyphPageMap = new HashMap<>();
    private GlyphFont glyphFont;
    private AbstractGlyphPage currentGlyphPage = null;
    private int currentScale = -1;

    public AbstractFontRenderer(GlyphFont glyphFont) {
        this.glyphFont = glyphFont;
    }

    public void setFont(GlyphFont glyphFont) {
        if(this.glyphFont == glyphFont) return;
        this.glyphFont = glyphFont;
        this.currentGlyphPage = null;
    }

    private AbstractGlyphPage getGlyphPage() {
        int scale = getScaleFactor();
        if(scale != currentScale || currentGlyphPage == null) {
            Map<Integer, AbstractGlyphPage> fontMap = glyphPageMap.computeIfAbsent(glyphFont, k -> new HashMap<>());
            currentGlyphPage = fontMap.get(scale);
            if(currentGlyphPage == null) {
                currentGlyphPage = createGlyphPage(glyphFont.getPath(), glyphFont.getSize() * scale / 2);
                fontMap.put(scale, currentGlyphPage);
            }
        }
        currentScale = scale;

        return currentGlyphPage;
    }

    protected abstract AbstractGlyphPage createGlyphPage(String path, int size);
    protected abstract int getScaleFactor();

    public double drawChar(char c, double x, double y, Color color) {
        return getGlyphPage().drawChar(c, x, y, color);
    }

    public void drawString(String text, double x, double y, Color color, boolean shadow) {
        getGlyphPage().drawString(text, x, y, color, shadow);
    }

    public void drawString(String text, double x, double y, Color color) {
        drawString(text, x, y, color, false);
    }

    public void drawStringWithShadow(String text, double x, double y, Color color) {
        drawString(text, x, y, color, true);
    }

    public int drawStringWithCustomWidthWithShadow(String text, double x, double y, Color color, double width) {
        return drawStringWithCustomWidth(text, x, y, color, width, true);
    }

    public int drawStringWithCustomWidth(String text, double x, double y, Color color, double width) {
        return drawStringWithCustomWidth(text, x, y, color, width, false);
    }

    public int drawStringWithCustomWidth(String text, double x, double y, Color color, double width, boolean shadow) {
        double scale = width / getStringWidth(text);
        RENDER_STACK.scale(scale, scale, 1);
        drawString(text, x / scale, y / scale, color, shadow);
        RENDER_STACK.scale(1 / scale, 1 / scale, 1);
        return (int) (getFontHeight() * scale);
    }

    public int drawStringWithCustomHeightWithShadow(String text, double x, double y, Color color, double height) {
        return drawStringWithCustomHeight(text, x, y, color, height, true);
    }

    public int drawStringWithCustomHeight(String text, double x, double y, Color color, double height) {
        return drawStringWithCustomHeight(text, x, y, color, height, false);
    }

    public int drawStringWithCustomHeight(String text, double x, double y, Color color, double height, boolean shadow) {
        double scale = height / getFontHeight();
        RENDER_STACK.scale(scale, scale, 1);
        drawString(text, x / scale, y / scale, color, shadow);
        RENDER_STACK.scale(1 / scale, 1 / scale, 1);

        return (int) (getStringWidth(text) * scale);
    }

    // returns height of split string
    public int drawSplitString(String text, double x, double y, Color color, double width) {
        return getGlyphPage().drawSplitString(text, x, y, color, width);
    }

    // draws split string with custom font height
    public double drawSplitString(String text, double x, double y, Color color, double width, double height) {
        double scale = height / getFontHeight();
        RENDER_STACK.scale(scale, scale, 1);
        height = drawSplitString(text, x / scale, y / scale, color, width / scale);
        RENDER_STACK.scale(1 / scale, 1 / scale, 1);

        return height * scale;
    }

    public double getCharWidth(char c) {
        return getGlyphPage().getCharWidth(c);
    }

    public double getStringWidth(String text) {
        return getGlyphPage().getStringWidth(text);
    }

    public double getStringWidth(String text, double height) {
        return getStringWidth(text) * (height / getGlyphPage().getFontHeight());
    }

    public int getFontHeight() {
        return getGlyphPage().getFontHeight();
    }

    public double getFontHeightWithCustomWidth(String text, double width) {
        return width / getStringWidth(text) * getFontHeight();
    }
}
