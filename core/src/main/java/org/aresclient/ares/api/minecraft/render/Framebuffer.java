package org.aresclient.ares.api.minecraft.render;

public interface Framebuffer {
    int getTextureWidth();
    int getTextureHeight();

    int getWidth();
    int getHeight();

    int getFBO();
    int getTexture();

    boolean isUsingDepth();

    int getDepthAttachment();

    int getFilter();
    void setFilter(int value);

    float[] getClearColor();
    void setClearColor(float r, float g, float b, float a);

    void bind(boolean updateViewport);
    void unbind();

    void bindTexture();
    void unbindTexture();

    void draw(int width, int height, boolean disableBlend);

    void clear();
}
