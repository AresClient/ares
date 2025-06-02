package org.aresclient.ares.mixin.mixins;

import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.render.*;
import net.minecraft.client.util.Handle;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.particle.ParticleEffect;
import org.aresclient.ares.impl.instrument.modules.render.NoRender;
import org.aresclient.ares.impl.instrument.modules.render.esp.OutlineESP;
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

    @Inject(method = "renderEntities", at = @At("HEAD"))
    public void renderEntitiesPre(MatrixStack matrices, VertexConsumerProvider.Immediate vertexConsumers, Camera camera, RenderTickCounter tickCounter, List<Entity> entities, CallbackInfo ci) {
        if(OutlineESP.INSTANCE.shouldRenderOutline()) {
            prevFramebuffer = entityOutlineFramebuffer;
            prevFramebufferHandle = framebufferSet.entityOutlineFramebuffer;
            entityOutlineFramebuffer = OutlineESP.INSTANCE.getFramebuffer();
            framebufferSet.entityOutlineFramebuffer = OutlineESP.INSTANCE::getFramebuffer;
        }
    }

    @ModifyArgs(method = "renderEntity", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/entity/EntityRenderDispatcher;render(Lnet/minecraft/entity/Entity;DDDFLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V"))
    public void dispatchRenderEntity(Args args) {
        Entity entity = args.get(0);
        if(OutlineESP.INSTANCE.shouldRenderOutline(entity)) {
            OutlineESP.INSTANCE.setColor(OutlineESP.INSTANCE.getLineColor(entity));
            args.set(6, OutlineESP.INSTANCE.getVertexConsumerProvider());
        }
    }

    @Inject(method = "renderEntities", at = @At("TAIL"))
    public void renderEntitiesPost(CallbackInfo ci) {
        if(OutlineESP.INSTANCE.shouldRenderOutline()) {
            OutlineESP.INSTANCE.getVertexConsumerProvider().draw();
            entityOutlineFramebuffer = prevFramebuffer;
            framebufferSet.entityOutlineFramebuffer = prevFramebufferHandle;
        }
    }

    @Inject(method = "drawEntityOutlinesFramebuffer", at = @At("HEAD"), cancellable = true)
    public void drawEntityOutlineFramebuffer(CallbackInfo ci) {
        if(OutlineESP.INSTANCE.shouldRenderOutline()) {
            OutlineESP.INSTANCE.blit();
            ci.cancel();
        }
    }

    @Inject(method = "onResized", at = @At("TAIL"))
    public void onResized(int width, int height, CallbackInfo ci) {
        OutlineESP.INSTANCE.getFramebuffer().resize(width, height);
    }

    // end outline ESP implementation

    @Inject(method = "spawnParticle(Lnet/minecraft/particle/ParticleEffect;ZZDDDDDD)Lnet/minecraft/client/particle/Particle;", at = @At("HEAD"), cancellable = true)
    public void spawnParticle(ParticleEffect parameters, boolean force, boolean canSpawnOnMinimal, double x, double y, double z, double velocityX, double velocityY, double velocityZ, CallbackInfoReturnable<Particle> cir) {
        if(NoRender.INSTANCE.shouldBlockParticles()) cir.setReturnValue(null);
    }
}
