package org.aresclient.ares.renderer;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL30;
import org.lwjgl.opengl.GL32;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public class MSAAFrameBuffer {
    private static final List<MSAAFrameBuffer> MSAAS = new ArrayList<>();
    private static final Buffer BUFFER = Buffer
            .createStatic(Shader.POSITION_TEXTURE, VertexFormat.POSITION_UV, 4, 6)
            .vertices(
                    -1, -1, 0, 0, 0,
                    -1, 1, 0, 0, 1,
                    1, 1, 0, 1, 1,
                    1, -1, 0, 1, 0
            )
            .indices(
                    0, 1, 2,
                    2, 0, 3
            );

    private final int framebuffer = GL30.glGenFramebuffers();
    private final int msTexture = GL11.glGenTextures();
    private final int rbo = GL30.glGenRenderbuffers();
    private final int intermediate = GL30.glGenFramebuffers();
    private final int texture = GL11.glGenTextures();
    private final int samples;
    private int width, height;

    public MSAAFrameBuffer(int samples, Resolution resolution) {
        this(samples, resolution.getScaledWidth(), resolution.getScaledHeight());
    }

    public MSAAFrameBuffer(int samples, int width, int height) {
        this.samples = Math.min(samples, GL11.glGetInteger(GL30.GL_MAX_SAMPLES));
        this.width = width;
        this.height = height;

        // create MSAA framebuffer and rbo
        GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, framebuffer);

        GL11.glBindTexture(GL32.GL_TEXTURE_2D_MULTISAMPLE, msTexture);
        GL32.glTexImage2DMultisample(GL32.GL_TEXTURE_2D_MULTISAMPLE, samples, GL11.GL_RGB, width, height, true);
        GL11.glBindTexture(GL32.GL_TEXTURE_2D_MULTISAMPLE, 0);
        GL30.glFramebufferTexture2D(GL30.GL_FRAMEBUFFER, GL30.GL_COLOR_ATTACHMENT0, GL32.GL_TEXTURE_2D_MULTISAMPLE, msTexture, 0);

        GL30.glBindRenderbuffer(GL30.GL_RENDERBUFFER, rbo);
        GL30.glRenderbufferStorageMultisample(GL30.GL_RENDERBUFFER, samples, GL30.GL_DEPTH24_STENCIL8, width, height);
        GL30.glBindRenderbuffer(GL30.GL_RENDERBUFFER, 0);
        GL30.glFramebufferRenderbuffer(GL30.GL_FRAMEBUFFER, GL30.GL_DEPTH_STENCIL_ATTACHMENT, GL30.GL_RENDERBUFFER, rbo);

        if(GL30.glCheckFramebufferStatus(GL30.GL_FRAMEBUFFER) != GL30.GL_FRAMEBUFFER_COMPLETE)
            throw new RuntimeException("Failed to create msaa framebuffer");
        GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, 0);

        // create intermediate buffer that renders texture of msaa frame buffer
        GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, intermediate);

        GL11.glBindTexture(GL11.GL_TEXTURE_2D, texture);
        GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGB, width, height, 0, GL11.GL_RGB, GL11.GL_UNSIGNED_BYTE, (ByteBuffer) null);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
        GL30.glFramebufferTexture2D(GL30.GL_FRAMEBUFFER, GL30.GL_COLOR_ATTACHMENT0, GL11.GL_TEXTURE_2D, texture, 0);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);

        int test;
        if((test = GL30.glCheckFramebufferStatus(GL30.GL_FRAMEBUFFER)) != GL30.GL_FRAMEBUFFER_COMPLETE)
            throw new RuntimeException("Failed to create intermediate msaa framebuffer " + test);
        GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, 0);

        MSAAS.add(this);
    }

    public void bind(int target) {
        GL30.glBindFramebuffer(GL30.GL_READ_FRAMEBUFFER, target);
        GL30.glBindFramebuffer(GL30.GL_DRAW_FRAMEBUFFER, framebuffer);
        GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
        GL30.glBlitFramebuffer(0, 0, width, height, 0, 0, width, height, GL11.GL_COLOR_BUFFER_BIT, GL11.GL_NEAREST);
    }

    public void blit(int target) {
        GL30.glBindFramebuffer(GL30.GL_READ_FRAMEBUFFER, framebuffer);
        GL30.glBindFramebuffer(GL30.GL_DRAW_FRAMEBUFFER, intermediate);
        GL30.glBlitFramebuffer(0, 0, width, height, 0, 0, width, height, GL11.GL_COLOR_BUFFER_BIT, GL11.GL_NEAREST);

        GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, target);
        GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);

        GL13.glActiveTexture(GL13.GL_TEXTURE0);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, texture);
        BUFFER.draw();
    }

    // must be called when screen is resized
    public void resize(int width, int height) {
        this.width = width;
        this.height = height;

        GL11.glBindTexture(GL32.GL_TEXTURE_2D_MULTISAMPLE, msTexture);
        GL32.glTexImage2DMultisample(GL32.GL_TEXTURE_2D_MULTISAMPLE, samples, GL11.GL_RGB, width, height, true);
        GL11.glBindTexture(GL32.GL_TEXTURE_2D_MULTISAMPLE, 0);

        GL30.glBindRenderbuffer(GL30.GL_RENDERBUFFER, rbo);
        GL30.glRenderbufferStorageMultisample(GL30.GL_RENDERBUFFER, samples, GL30.GL_DEPTH24_STENCIL8, width, height);
        GL30.glBindRenderbuffer(GL30.GL_RENDERBUFFER, 0);

        GL11.glBindTexture(GL11.GL_TEXTURE_2D, texture);
        GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGB, width, height, 0, GL11.GL_RGB, GL11.GL_UNSIGNED_BYTE, (ByteBuffer) null);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);
    }

    public void resize(Resolution resolution) {
        resize(resolution.getWidth(), resolution.getHeight());
    }

    public void delete() {
        GL11.glDeleteTextures(texture);
        GL30.glDeleteFramebuffers(framebuffer);
        GL30.glDeleteFramebuffers(intermediate);
        MSAAS.remove(this);
    }

    public static void clear() {
        for(MSAAFrameBuffer msaa: MSAAS) {
            GL11.glDeleteTextures(msaa.texture);
            GL30.glDeleteFramebuffers(msaa.framebuffer);
            GL30.glDeleteFramebuffers(msaa.intermediate);
        }
        MSAAS.clear();
    }
}
