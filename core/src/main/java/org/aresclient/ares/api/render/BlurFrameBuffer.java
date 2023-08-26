package org.aresclient.ares.api.render;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL30;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public class BlurFrameBuffer {
    private static final List<BlurFrameBuffer> BLURS = new ArrayList<>();
    private static final Shader SHADER = Shader.fromResources("/assets/ares/shaders/vert/blur.vert", "/assets/ares/shaders/frag/blur.frag");
    private static final Uniform.F2 RESOLUTION = SHADER.uniformF2("resolution");
    private static final Uniform.F2 DIRECTION = SHADER.uniformF2("direction");
    private static final Buffer BUFFER = Buffer
            .createStatic(SHADER, VertexFormat.POSITION_UV, 4, 6)
            .vertices(
                    1, 1, 0,    1, 1,
                    1, -1, 0,   1, 0,
                    -1, 1, 0,   0, 1,
                    -1, -1, 0,  0, 0
            )
            .indices(
                    0, 1, 2,
                    1, 2, 3
            )
            .uniform(RESOLUTION)
            .uniform(DIRECTION);

    private final int framebuffer = GL30.glGenFramebuffers();
    private final int first = GL11.glGenTextures();
    private final int second = GL11.glGenTextures();
    private int width, height;

    public BlurFrameBuffer(Resolution resolution) {
        this(resolution.getWidth(), resolution.getHeight());
    }

    public BlurFrameBuffer(int width, int height) {
        this.width = width;
        this.height = height;

        int drawFBO = GL30.glGetInteger(GL30.GL_DRAW_FRAMEBUFFER_BINDING);
        int readFBO = GL30.glGetInteger(GL30.GL_READ_FRAMEBUFFER_BINDING);
        GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, framebuffer);

        genTexture(first);
        genTexture(second);

        if(GL30.glCheckFramebufferStatus(GL30.GL_FRAMEBUFFER) != GL30.GL_FRAMEBUFFER_COMPLETE)
            throw new RuntimeException("Failed to create blur framebuffer");
        GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, 0);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);

        GL30.glBindFramebuffer(GL30.GL_DRAW_FRAMEBUFFER, drawFBO);
        GL30.glBindFramebuffer(GL30.GL_READ_FRAMEBUFFER, readFBO);

        BLURS.add(this);
    }

    private void genTexture(int id) {
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, id);
        GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGB, width, height, 0, GL11.GL_RGB, GL11.GL_UNSIGNED_BYTE, (ByteBuffer) null);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
        GL30.glFramebufferTexture2D(GL30.GL_FRAMEBUFFER, GL30.GL_COLOR_ATTACHMENT0, GL11.GL_TEXTURE_2D, id, 0);
    }

    public void render(float rx, float ry) {
        RESOLUTION.set(width, height);

        // IMPORTANT!! setup viewport to be same size as framebuffer, and keep track of prev size
        int[] viewport = Buffer.getViewport();
        GL11.glViewport(0, 0, width, height); // this took me so long to figure out :(

        int drawFBO = GL30.glGetInteger(GL30.GL_DRAW_FRAMEBUFFER_BINDING);
        int readFBO = GL30.glGetInteger(GL30.GL_READ_FRAMEBUFFER_BINDING);
        GL30.glBindFramebuffer(GL30.GL_READ_FRAMEBUFFER, drawFBO);
        GL30.glBindFramebuffer(GL30.GL_DRAW_FRAMEBUFFER, framebuffer);

        // blit from main framebuffer to blur framebuffer
        GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
        GL30.glFramebufferTexture2D(GL30.GL_FRAMEBUFFER, GL30.GL_COLOR_ATTACHMENT0, GL11.GL_TEXTURE_2D, first, 0);
        GL30.glBlitFramebuffer(0, 0, width, height, 0, 0, width, height, GL11.GL_COLOR_BUFFER_BIT, GL11.GL_NEAREST);

        // first pass, writing back to framebuffer
        GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, framebuffer);
        GL13.glActiveTexture(GL13.GL_TEXTURE0);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, first);
        GL30.glFramebufferTexture2D(GL30.GL_FRAMEBUFFER, GL30.GL_COLOR_ATTACHMENT0, GL11.GL_TEXTURE_2D, second, 0);
        DIRECTION.set(rx, 0);
        BUFFER.draw();

        // second pass, render onto main buffer
        GL30.glBindFramebuffer(GL30.GL_DRAW_FRAMEBUFFER, drawFBO);
        GL30.glBindFramebuffer(GL30.GL_READ_FRAMEBUFFER, readFBO);
        GL11.glViewport(viewport[0], viewport[1], viewport[2], viewport[3]); // reset viewport for rendering to main buffer
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, second);
        DIRECTION.set(0, ry);
        BUFFER.draw();
    }

    public void resize(int width, int height) {
        boolean update = width != this.width || height != this.height;
        if(!update) return;

        this.width = width;
        this.height = height;

        int drawFBO = GL30.glGetInteger(GL30.GL_DRAW_FRAMEBUFFER_BINDING);
        int readFBO = GL30.glGetInteger(GL30.GL_READ_FRAMEBUFFER_BINDING);
        GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, framebuffer);

        genTexture(first);
        genTexture(second);

        GL30.glBindFramebuffer(GL30.GL_DRAW_FRAMEBUFFER, drawFBO);
        GL30.glBindFramebuffer(GL30.GL_READ_FRAMEBUFFER, readFBO);
    }

    public void resize(Resolution resolution) {
        resize(resolution.getWidth(), resolution.getHeight());
    }

    public void delete() {
        GL11.glDeleteTextures(first);
        GL11.glDeleteTextures(second);
        GL30.glDeleteFramebuffers(framebuffer);
        BLURS.remove(this);
    }

    public static void clear() {
        for(BlurFrameBuffer blur: BLURS) {
            GL11.glDeleteTextures(blur.first);
            GL11.glDeleteTextures(blur.second);
            GL30.glDeleteFramebuffers(blur.framebuffer);
        }
        BLURS.clear();
    }
}
