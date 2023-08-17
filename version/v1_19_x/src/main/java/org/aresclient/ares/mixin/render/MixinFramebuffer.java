package org.aresclient.ares.mixin.render;

import net.minecraft.client.gl.Framebuffer;
import org.lwjgl.opengl.GL30;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

@Mixin(Framebuffer.class)
public abstract class MixinFramebuffer implements org.aresclient.ares.api.minecraft.render.Framebuffer {
    @Shadow public int textureWidth;

    @Shadow public int textureHeight;

    @Shadow public int viewportWidth;

    @Shadow public int viewportHeight;

    @Shadow public int fbo;

    @Shadow public int texFilter;

    @Shadow @Final public boolean useDepthAttachment;

    @Shadow protected int depthAttachment;

    @Shadow protected int colorAttachment;

    @Shadow @Final private float[] clearColor;

    @Shadow public abstract void beginWrite(boolean setViewport);

    @Shadow public abstract void endWrite();

    @Shadow public abstract void beginRead();

    @Shadow public abstract void endRead();

    @Shadow public abstract void clear(boolean getError);

    @Shadow public abstract void resize(int width, int height, boolean getError);

    @Shadow public abstract void setTexFilter(int $$0);

    @Override
    public int getTextureWidth() {
        return this.textureWidth;
    }

    @Override
    public int getTextureHeight() {
        return this.textureHeight;
    }

    @Override
    public int getWidth() {
        return this.viewportWidth;
    }

    @Override
    public int getHeight() {
        return this.viewportHeight;
    }

    @Override
    public int getFBO() {
        return this.fbo;
    }

    @Override
    public int getTexture() {
        return this.colorAttachment;
    }

    @Override
    public boolean isUsingDepth() {
        return this.useDepthAttachment;
    }

    @Override
    public int getDepthAttachment() {
        return this.depthAttachment;
    }

    @Override
    public float[] getClearColor() {
        return this.clearColor;
    }

    @Override
    public int getFilter() {
        return this.texFilter;
    }

    @Override
    public void setFilter(int value) {
        this.setTexFilter(value);
    }

    @Override
    public void setClearColor(float r, float g, float b, float a) {
        clearColor[0] = r;
        clearColor[1] = g;
        clearColor[2] = b;
        clearColor[3] = a;
    }

    @Override
    public void bind(boolean updateViewport) {
        this.beginWrite(updateViewport);
    }

    @Override
    public void unbind() {
        this.endWrite();
    }

    @Override
    public void bindTexture() {
        this.beginRead();
    }

    @Override
    public void unbindTexture() {
        this.endRead();
    }

    @Override
    public void clear() {
        this.clear(false);
    }

    // enable stencil testing
    @ModifyArgs(method = "initFbo", at = @At(value = "INVOKE",
            target = "Lcom/mojang/blaze3d/platform/GlStateManager;_texImage2D(IIIIIIIILjava/nio/IntBuffer;)V", ordinal = 0))
    public void stencil(Args args) {
        args.set(2, GL30.GL_DEPTH32F_STENCIL8);
        args.set(6, GL30.GL_DEPTH_STENCIL);
        args.set(7, GL30.GL_FLOAT_32_UNSIGNED_INT_24_8_REV);
    }

    @ModifyArgs(method = "initFbo",
            at = @At (value = "INVOKE", target = "Lcom/mojang/blaze3d/platform/GlStateManager;_glFramebufferTexture2D(IIIII)V"),
            slice = @Slice(from = @At (value = "FIELD", target = "Lnet/minecraft/client/gl/Framebuffer;useDepthAttachment:Z", ordinal = 1)))
    public void stencil1(Args args) {
        args.set(1, GL30.GL_DEPTH_STENCIL_ATTACHMENT);
    }
}
