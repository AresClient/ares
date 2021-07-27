package dev.tigr.ares.core.util.render.font;

/**
 * @author Tigermouthbear 7/27/21
 * represents a font which can be used in a font renderer
 */
public class GlyphFont {
    private final String path;
    private final int size;

    public GlyphFont(String path, int size) {
        this.path = path;
        this.size = size;
    }

    public String getPath() {
        return path;
    }

    public int getSize() {
        return size;
    }
}
