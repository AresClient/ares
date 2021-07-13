package dev.tigr.ares.core.util.render;

public interface IFontRenderer {
    double drawChar(char c, double x, double y, Color color);

    void drawString(String text, double x, double y, Color color, boolean shadow);

    void drawString(String text, double x, double y, Color color);

    void drawStringWithShadow(String text, double x, double y, Color color);

    int drawStringWithCustomWidthWithShadow(String text, double x, double y, Color color, double width);

    int drawStringWithCustomWidth(String text, double x, double y, Color color, double width);

    int drawStringWithCustomWidth(String text, double x, double y, Color color, double width, boolean shadow);

    int drawStringWithCustomHeightWithShadow(String text, double x, double y, Color color, double height);

    int drawStringWithCustomHeight(String text, double x, double y, Color color, double height);

    int drawStringWithCustomHeight(String text, double x, double y, Color color, double height, boolean shadow);

    int drawSplitString(String text, double x, double y, Color color, double width);

    double drawSplitString(String text, double x, double y, Color color, double width, double height);

    double getCharWidth(char c);

    double getStringWidth(String text);

    double getStringWidth(String text, double height);

    int getFontHeight();

    double getFontHeightWithCustomWidth(String text, double width);
}
