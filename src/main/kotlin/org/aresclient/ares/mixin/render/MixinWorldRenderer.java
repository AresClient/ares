package org.aresclient.ares.mixin.render;

import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.render.*;
import net.minecraft.client.util.Handle;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import org.aresclient.ares.impl.instrument.module.modules.render.esp.ESP;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(WorldRenderer.class)
public abstract class MixinWorldRenderer {
    // begin outline ESP outline implementation

    @Shadow private Framebuffer entityOutlineFramebuffer;
    @Final @Shadow private DefaultFramebufferSet framebufferSet;

    @Unique Framebuffer prevFramebuffer;
    @Unique Handle<Framebuffer> prevFramebufferHandle;

    @Inject(method = "renderEntity", at = @At("HEAD"))
    public void renderEntityPre(Entity entity, double cameraX, double cameraY, double cameraZ, float tickProgress, MatrixStack matrices, VertexConsumerProvider vertexConsumers, CallbackInfo ci) {
        if(ESP.INSTANCE.shouldRenderOutline()) {
            prevFramebuffer = entityOutlineFramebuffer;
            prevFramebufferHandle = framebufferSet.entityOutlineFramebuffer;
            entityOutlineFramebuffer = ESP.Outliner.INSTANCE.getFramebuffer();
            framebufferSet.entityOutlineFramebuffer = ESP.Outliner.INSTANCE::getFramebuffer;

            ESP.Outliner.INSTANCE.setColor(ESP.INSTANCE.getEntityColor(entity));
        }
    }

    @ModifyArg(method = "renderEntity", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/entity/EntityRenderDispatcher;render(Lnet/minecraft/entity/Entity;DDDFLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V"), index = 6)
    public VertexConsumerProvider dispatchRenderEntity(VertexConsumerProvider vertexConsumerProvider) {
        if(ESP.INSTANCE.shouldRenderOutline()) return ESP.Outliner.INSTANCE.getVertexConsumerProvider();
        else return vertexConsumerProvider;
    }

    @Inject(method = "renderEntity", at = @At("TAIL"))
    public void renderEntityPost(Entity entity, double cameraX, double cameraY, double cameraZ, float tickProgress, MatrixStack matrices, VertexConsumerProvider vertexConsumers, CallbackInfo ci) {
        if(ESP.INSTANCE.shouldRenderOutline()) {
            ESP.Outliner.INSTANCE.getVertexConsumerProvider().draw();
            entityOutlineFramebuffer = prevFramebuffer;
            framebufferSet.entityOutlineFramebuffer = prevFramebufferHandle;
        }
    }

    @Inject(method = "drawEntityOutlinesFramebuffer", at = @At("TAIL"))
    public void drawEntityOutlineFramebuffer(CallbackInfo ci) {
        if(ESP.INSTANCE.shouldRenderOutline()) ESP.Outliner.INSTANCE.blit();
    }

    @Inject(method = "onResized", at = @At("TAIL"))
    public void onResized(int width, int height, CallbackInfo ci) {
        ESP.Outliner.INSTANCE.getFramebuffer().resize(width, height);
    }

    // end outline ESP implementation
}
