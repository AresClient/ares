package org.aresclient.ares.mixin.mixins;

import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.render.*;
import net.minecraft.client.util.Handle;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import org.aresclient.ares.impl.instrument.module.modules.render.ESP;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

import java.util.List;

@Mixin(WorldRenderer.class)
public abstract class MixinWorldRenderer {
    // begin outline ESP outline implementation
    // see also MixinEntity

    @Shadow private Framebuffer entityOutlineFramebuffer;
    @Final @Shadow private DefaultFramebufferSet framebufferSet;

    @Unique Framebuffer prevFramebuffer;
    @Unique Handle<Framebuffer> prevFramebufferHandle;

    @Inject(method = "getEntitiesToRender", at = @At("TAIL"), cancellable = true)
    private void getEntitiesToRender(CallbackInfoReturnable<Boolean> cir) {
        if(ESP.INSTANCE.shouldRenderOutline()) cir.setReturnValue(true);
    }

    @Inject(method = "renderEntities", at = @At("HEAD"))
    public void renderEntitiesPre(MatrixStack matrices, VertexConsumerProvider.Immediate vertexConsumers, Camera camera, RenderTickCounter tickCounter, List<Entity> entities, CallbackInfo ci) {
        if(ESP.INSTANCE.shouldRenderOutline()) {
            prevFramebuffer = entityOutlineFramebuffer;
            prevFramebufferHandle = framebufferSet.entityOutlineFramebuffer;
            entityOutlineFramebuffer = ESP.Outliner.INSTANCE.getFramebuffer();
            framebufferSet.entityOutlineFramebuffer = ESP.Outliner.INSTANCE::getFramebuffer;
        }
    }

    @ModifyArgs(method = "renderEntity", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/entity/EntityRenderDispatcher;render(Lnet/minecraft/entity/Entity;DDDFLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V"))
    public void dispatchRenderEntity(Args args) {
        Entity entity = args.get(0);
        if(ESP.INSTANCE.shouldRenderOutline(entity)) {
            ESP.Outliner.INSTANCE.setColor(ESP.INSTANCE.getEntityColor(entity));
            args.set(6, ESP.Outliner.INSTANCE.getVertexConsumerProvider());
        }
    }

    @Inject(method = "method_62214", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/OutlineVertexConsumerProvider;draw()V"))
    public void drawOutlineVertices(CallbackInfo ci) {
        if(ESP.INSTANCE.shouldRenderOutline()) ESP.Outliner.INSTANCE.getVertexConsumerProvider().draw();
    }

    @Inject(method = "renderEntities", at = @At("TAIL"))
    public void renderEntitiesPost(CallbackInfo ci) {
        if(ESP.INSTANCE.shouldRenderOutline()) {
            entityOutlineFramebuffer = prevFramebuffer;
            framebufferSet.entityOutlineFramebuffer = prevFramebufferHandle;
        }
    }

    @Inject(method = "drawEntityOutlinesFramebuffer", at = @At("HEAD"), cancellable = true)
    public void drawEntityOutlineFramebuffer(CallbackInfo ci) {
        if(ESP.INSTANCE.shouldRenderOutline()) {
            ESP.Outliner.INSTANCE.blit();
            ci.cancel();
        }
    }

    @Inject(method = "onResized", at = @At("TAIL"))
    public void onResized(int width, int height, CallbackInfo ci) {
        ESP.Outliner.INSTANCE.getFramebuffer().resize(width, height);
    }

    // end outline ESP implementation
}
