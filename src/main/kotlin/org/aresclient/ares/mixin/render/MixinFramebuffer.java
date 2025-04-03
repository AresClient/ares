package org.aresclient.ares.mixin.render;

import net.minecraft.client.gl.Framebuffer;
import org.lwjgl.opengl.GL30;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

//TODO: FIX?
@Mixin(Framebuffer.class)
public abstract class MixinFramebuffer {
    // enable stencil testing
//    @ModifyArgs(method = "initFbo", at = @At(value = "INVOKE",
//            target = "Lcom/mojang/blaze3d/platform/GlStateManager;_texImage2D(IIIIIIIILjava/nio/IntBuffer;)V", ordinal = 0))
//    public void stencil(Args args) {
//        args.set(2, GL30.GL_DEPTH32F_STENCIL8);
//        args.set(6, GL30.GL_DEPTH_STENCIL);
//        args.set(7, GL30.GL_FLOAT_32_UNSIGNED_INT_24_8_REV);
//    }
//
//    @ModifyArgs(method = "initFbo",
//            at = @At (value = "INVOKE", target = "Lcom/mojang/blaze3d/platform/GlStateManager;_glFramebufferTexture2D(IIIII)V"),
//            slice = @Slice(from = @At (value = "FIELD", target = "Lnet/minecraft/client/gl/Framebuffer;useDepthAttachment:Z", ordinal = 1)))
//    public void stencil1(Args args) {
//        args.set(1, GL30.GL_DEPTH_STENCIL_ATTACHMENT);
//    }
}
