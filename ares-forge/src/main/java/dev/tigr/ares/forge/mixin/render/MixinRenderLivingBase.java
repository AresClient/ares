package dev.tigr.ares.forge.mixin.render;

import dev.tigr.ares.core.Ares;
import dev.tigr.ares.forge.event.events.render.RenderNametagsEvent;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.RenderLivingBase;
import net.minecraft.entity.EntityLivingBase;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * @author Tigermouthbear
 */
@Mixin(RenderLivingBase.class)
public class MixinRenderLivingBase<T extends EntityLivingBase> {
    @Inject(method = "renderName", at = @At("HEAD"), cancellable = true)
    public void renderNameHead(T entity, double x, double y, double z, CallbackInfo ci) {
        if(entity instanceof AbstractClientPlayer && Ares.EVENT_MANAGER.post(new RenderNametagsEvent()).isCancelled())
            ci.cancel();
    }
}
