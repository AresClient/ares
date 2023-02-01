package org.aresclient.ares.api;

import org.aresclient.ares.renderer.Color;

public interface TextRenderer {
    void drawText(String text, float x, float y, Color color);
    void drawTextWithShadow(String text, float x, float y, Color color);
}
