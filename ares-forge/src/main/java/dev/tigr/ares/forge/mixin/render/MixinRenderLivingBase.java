package dev.tigr.ares.forge.mixin.render;

import dev.tigr.ares.core.Ares;
import dev.tigr.ares.forge.event.events.render.PlayerModelRenderEvent;
import dev.tigr.ares.forge.event.events.render.RenderNametagsEvent;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.renderer.entity.RenderLivingBase;
import net.minecraft.entity.EntityLivingBase;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * @author Tigermouthbear
 * @author Makrennel 10/13/21 RenderModel Rotation
 */
@Mixin(RenderLivingBase.class)
public abstract class MixinRenderLivingBase<T extends EntityLivingBase> {

    @Shadow protected ModelBase mainModel;

    @Inject(method = "renderName", at = @At("HEAD"), cancellable = true)
    public void renderNameHead(T entity, double x, double y, double z, CallbackInfo ci) {
        if(entity instanceof AbstractClientPlayer && Ares.EVENT_MANAGER.post(new RenderNametagsEvent()).isCancelled())
            ci.cancel();
    }

    @Inject(method = "renderModel", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/model/ModelBase;render(Lnet/minecraft/entity/Entity;FFFFFF)V"), cancellable = true)
    public void onRenderModel(T entitylivingbaseIn, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scaleFactor, CallbackInfo ci) {
        PlayerModelRenderEvent event = new PlayerModelRenderEvent(entitylivingbaseIn);
        if(!Ares.EVENT_MANAGER.post(event).isCancelled()) return;

        ci.cancel();

        mainModel.render(entitylivingbaseIn, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, event.getHeadPitch(), scaleFactor);
    }
}
