package org.aresclient.ares.api.render;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;

import java.util.ArrayList;
import java.util.List;

public abstract class CustomFramebuffer {
    private static final List<CustomFramebuffer> BUFFERS = new ArrayList<>();

    private final int framebuffer = GL30.glGenFramebuffers();
    private final int first = GL11.glGenTextures();
    private final int second = GL11.glGenTextures();
    private int width, height;

    public CustomFramebuffer(int width, int height) {
        this.width = width;
        this.height = height;

        int drawFBO = GL30.glGetInteger(GL30.GL_DRAW_FRAMEBUFFER_BINDING);
        int readFBO = GL30.glGetInteger(GL30.GL_READ_FRAMEBUFFER_BINDING);
        GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, framebuffer);

        genTextures();

        if(GL30.glCheckFramebufferStatus(GL30.GL_FRAMEBUFFER) != GL30.GL_FRAMEBUFFER_COMPLETE)
            throw new RuntimeException("Failed to create custom framebuffer");

        GL30.glBindFramebuffer(GL30.GL_DRAW_FRAMEBUFFER, drawFBO);
        GL30.glBindFramebuffer(GL30.GL_READ_FRAMEBUFFER, readFBO);

        BUFFERS.add(this);
    }

    protected abstract void genTextures();

    public void resize(int width, int height) {
        boolean update = width != this.width || height != this.height;
        if(!update) return;

        this.width = width;
        this.height = height;

        int drawFBO = GL30.glGetInteger(GL30.GL_DRAW_FRAMEBUFFER_BINDING);
        int readFBO = GL30.glGetInteger(GL30.GL_READ_FRAMEBUFFER_BINDING);
        GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, framebuffer);

        genTextures();

        GL30.glBindFramebuffer(GL30.GL_DRAW_FRAMEBUFFER, drawFBO);
        GL30.glBindFramebuffer(GL30.GL_READ_FRAMEBUFFER, readFBO);
    }

    public int getFramebuffer() {
        return framebuffer;
    }

    public int getFirstTexture() {
        return first;
    }

    public int getSecondTexture() {
        return second;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public void delete() {
        GL11.glDeleteTextures(first);
        GL11.glDeleteTextures(second);
        GL30.glDeleteFramebuffers(framebuffer);
    }

    public static void clear() {
        for(CustomFramebuffer buffer: BUFFERS) {
            buffer.delete();
            GL11.glDeleteTextures(buffer.first);
            GL11.glDeleteTextures(buffer.second);
            GL30.glDeleteFramebuffers(buffer.framebuffer);
        }
        BUFFERS.clear();
    }
}
