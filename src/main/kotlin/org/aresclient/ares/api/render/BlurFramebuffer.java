package org.aresclient.ares.api.render;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL30;

import java.nio.ByteBuffer;

public class BlurFramebuffer extends CustomFramebuffer {
    private static final Shader SHADER = Shader.fromResources("/assets/ares/shaders/vert/blur.vert", "/assets/ares/shaders/frag/blur.frag");
    private static final Uniform.F2 RESOLUTION = SHADER.uniformF2("resolution");
    private static final Uniform.F2 DIRECTION = SHADER.uniformF2("direction");
    private static final Buffer BUFFER = Buffer
            .createStatic(SHADER, VertexFormat.POSITION_UV, 4, 6)
            .vertices(
                    1, 1, 0,    1, 1,
                    1, -1, 0,         1, 0,
                    -1, 1, 0,         0, 1,
                    -1, -1, 0,        0, 0
            )
            .indices(
                    0, 1, 2,
                    1, 2, 3
            )
            .uniform(RESOLUTION)
            .uniform(DIRECTION);

    public BlurFramebuffer(int width, int height) {
        super(width, height);
    }

    @Override
    protected void genTextures() {
        genTexture(getFirstTexture());
        genTexture(getSecondTexture());
    }

    private void genTexture(int id) {
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, id);
        GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGB, getWidth(), getHeight(), 0, GL11.GL_RGB, GL11.GL_UNSIGNED_BYTE, (ByteBuffer) null);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
        GL30.glFramebufferTexture2D(GL30.GL_FRAMEBUFFER, GL30.GL_COLOR_ATTACHMENT0, GL11.GL_TEXTURE_2D, id, 0);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);
    }

    public void render(float rx, float ry) {
        RESOLUTION.set(getWidth(), getHeight());

        // TODO: resizing still kinda broken
        // IMPORTANT!! setup viewport to be same size as framebuffer, and keep track of prev size
        int[] viewport = new int[4];
        GL11.glGetIntegerv(GL30.GL_VIEWPORT, viewport);
        GL11.glViewport(0, 0, getWidth(), getHeight());

        int drawFBO = GL30.glGetInteger(GL30.GL_DRAW_FRAMEBUFFER_BINDING);
        int readFBO = GL30.glGetInteger(GL30.GL_READ_FRAMEBUFFER_BINDING);
        GL30.glBindFramebuffer(GL30.GL_READ_FRAMEBUFFER, drawFBO);
        GL30.glBindFramebuffer(GL30.GL_DRAW_FRAMEBUFFER, getFramebuffer());

        // blit from main framebuffer to blur framebuffer
        GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
        GL30.glFramebufferTexture2D(GL30.GL_FRAMEBUFFER, GL30.GL_COLOR_ATTACHMENT0, GL11.GL_TEXTURE_2D, getFirstTexture(), 0);
        GL30.glBlitFramebuffer(0, 0, getWidth(), getHeight(), 0, 0, getWidth(), getHeight(), GL11.GL_COLOR_BUFFER_BIT, GL11.GL_NEAREST);

        // first pass, writing back to framebuffer
        GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, getFramebuffer());
        GL13.glActiveTexture(GL13.GL_TEXTURE0);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, getFirstTexture());
        GL30.glFramebufferTexture2D(GL30.GL_FRAMEBUFFER, GL30.GL_COLOR_ATTACHMENT0, GL11.GL_TEXTURE_2D, getSecondTexture(), 0);
        DIRECTION.set(rx, 0);
        BUFFER.draw();

        // second pass, render onto main buffer
        GL30.glBindFramebuffer(GL30.GL_DRAW_FRAMEBUFFER, drawFBO);
        GL30.glBindFramebuffer(GL30.GL_READ_FRAMEBUFFER, readFBO);
        GL11.glViewport(viewport[0], viewport[1], viewport[2], viewport[3]); // reset viewport for rendering to main buffer
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, getSecondTexture());
        DIRECTION.set(0, ry);
        BUFFER.draw();
        GL11.glBindTexture(GL30.GL_TEXTURE_2D, 0);
    }
}
