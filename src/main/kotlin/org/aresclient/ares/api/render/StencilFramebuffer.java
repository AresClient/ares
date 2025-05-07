package org.aresclient.ares.api.render;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;

import java.nio.ByteBuffer;

public class StencilFramebuffer extends CustomFramebuffer {
    private static final Buffer BUFFER = Buffer
            .createStatic(Shader.POSITION_TEXTURE, VertexFormat.POSITION_UV, 4, 6)
            .vertices(
                    1, 1, 0,    1, 1,
                    1, -1, 0,         1, 0,
                    -1, 1, 0,         0, 1,
                    -1, -1, 0,        0, 0
            )
            .indices(
                    0, 1, 2,
                    1, 2, 3
            );

    public StencilFramebuffer(int width, int height) {
        super(width, height);
    }

    @Override
    protected void genTextures() {
        // color
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, getFirstTexture());
        GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA8, getWidth(), getHeight(), 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, (ByteBuffer) null);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
        GL30.glFramebufferTexture2D(GL30.GL_FRAMEBUFFER, GL30.GL_COLOR_ATTACHMENT0, GL11.GL_TEXTURE_2D, getFirstTexture(), 0);

        // stencil + depth
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, getSecondTexture());
        GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL30.GL_DEPTH32F_STENCIL8, getWidth(), getHeight(), 0, GL30.GL_DEPTH_STENCIL, GL30.GL_FLOAT_32_UNSIGNED_INT_24_8_REV, (ByteBuffer) null);
        GL30.glFramebufferTexture2D(GL30.GL_FRAMEBUFFER, GL30.GL_DEPTH_STENCIL_ATTACHMENT, GL11.GL_TEXTURE_2D, getSecondTexture(), 0);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);
    }

    public void use(Runnable runnable) {
        int drawFBO = GL30.glGetInteger(GL30.GL_DRAW_FRAMEBUFFER_BINDING);
        int readFBO = GL30.glGetInteger(GL30.GL_READ_FRAMEBUFFER_BINDING);
        GL30.glBindFramebuffer(GL30.GL_READ_FRAMEBUFFER, drawFBO);
        GL30.glBindFramebuffer(GL30.GL_DRAW_FRAMEBUFFER, getFramebuffer());

        GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
        GL11.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
        GL30.glBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL30.glFramebufferTexture2D(GL30.GL_FRAMEBUFFER, GL30.GL_COLOR_ATTACHMENT0, GL11.GL_TEXTURE_2D, getFirstTexture(), 0);
        GL30.glBlitFramebuffer(0, 0, getWidth(), getHeight(), 0, 0, getWidth(), getHeight(), GL11.GL_COLOR_BUFFER_BIT, GL11.GL_NEAREST);

        runnable.run();

        GL30.glBindFramebuffer(GL30.GL_DRAW_FRAMEBUFFER, drawFBO);
        GL30.glBindFramebuffer(GL30.GL_READ_FRAMEBUFFER, readFBO);

        GL11.glBindTexture(GL11.GL_TEXTURE_2D, getFirstTexture());
        BUFFER.draw();
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);
        GL30.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
    }
}
