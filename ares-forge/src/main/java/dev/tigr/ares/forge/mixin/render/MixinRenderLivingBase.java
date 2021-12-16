package dev.tigr.ares.forge.mixin.render;

import dev.tigr.ares.core.Ares;
import dev.tigr.ares.core.event.render.PlayerModelRenderEvent;
import dev.tigr.ares.forge.event.events.render.RenderNametagsEvent;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderLivingBase;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.MathHelper;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.Final;
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
public abstract class MixinRenderLivingBase<T extends EntityLivingBase> extends Render<T> {

    @Shadow protected ModelBase mainModel;

    protected MixinRenderLivingBase(RenderManager renderManager) {
        super(renderManager);
    }

    @Shadow protected abstract float getSwingProgress(T livingBase, float partialTickTime);

    @Shadow protected abstract float interpolateRotation(float prevYawOffset, float yawOffset, float partialTicks);

    @Shadow protected abstract void renderLivingAt(T entityLivingBaseIn, double x, double y, double z);

    @Shadow protected abstract float handleRotationFloat(T livingBase, float partialTicks);

    @Shadow protected abstract void applyRotations(T entityLiving, float ageInTicks, float rotationYaw, float partialTicks);

    @Shadow public abstract float prepareScale(T entitylivingbaseIn, float partialTicks);

    @Shadow protected abstract boolean setScoreTeamColor(T entityLivingBaseIn);

    @Shadow protected abstract void renderModel(T entitylivingbaseIn, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scaleFactor);

    @Shadow protected abstract void renderLayers(T entitylivingbaseIn, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch, float scaleIn);

    @Shadow protected abstract void unsetScoreTeamColor();

    @Shadow protected abstract boolean setDoRenderBrightness(T entityLivingBaseIn, float partialTicks);

    @Shadow protected abstract void unsetBrightness();

    @Shadow @Final private static Logger LOGGER;

    @Shadow protected boolean renderMarker;

    @Inject(method = "renderName", at = @At("HEAD"), cancellable = true)
    public void renderNameHead(T entity, double x, double y, double z, CallbackInfo ci) {
        if(entity instanceof AbstractClientPlayer && Ares.EVENT_MANAGER.post(new RenderNametagsEvent()).isCancelled())
            ci.cancel();
    }

    @Inject(method = "doRender(Lnet/minecraft/entity/EntityLivingBase;DDDFF)V", at = @At("HEAD"), cancellable = true)
    public void onRender(T entity, double x, double y, double z, float entityYaw, float partialTicks, CallbackInfo ci) {
        PlayerModelRenderEvent event = new PlayerModelRenderEvent(entity.getEntityId(), entity.rotationPitch, entity.rotationYawHead, entity.renderYawOffset);
        if(!Ares.EVENT_MANAGER.post(event).isCancelled()) {
            return;
        }

        ci.cancel();

        GlStateManager.pushMatrix();
        GlStateManager.disableCull();
        mainModel.swingProgress = getSwingProgress(entity, partialTicks);
        boolean shouldSit = entity.isRiding() && (entity.getRidingEntity() != null && entity.getRidingEntity().shouldRiderSit());
        mainModel.isRiding = shouldSit;
        mainModel.isChild = entity.isChild();

        try
        {
            entity.setRotationYawHead(event.getHeadYaw());
            entity.setRenderYawOffset(event.getBodyYaw());
            float f = interpolateRotation(entity.prevRenderYawOffset, entity.renderYawOffset, partialTicks);
            float f1 = interpolateRotation(entity.prevRotationYawHead, entity.rotationYawHead, partialTicks);
            float f2 = f1 - f;

            if (shouldSit && entity.getRidingEntity() instanceof EntityLivingBase)
            {
                EntityLivingBase entitylivingbase = (EntityLivingBase)entity.getRidingEntity();
                f = interpolateRotation(entitylivingbase.prevRenderYawOffset, entitylivingbase.renderYawOffset, partialTicks);
                f2 = f1 - f;
                float f3 = MathHelper.wrapDegrees(f2);

                if (f3 < -85.0F)
                {
                    f3 = -85.0F;
                }

                if (f3 >= 85.0F)
                {
                    f3 = 85.0F;
                }

                f = f1 - f3;

                if (f3 * f3 > 2500.0F)
                {
                    f += f3 * 0.2F;
                }

                f2 = f1 - f;
            }

//            float f7 = entity.prevRotationPitch + (event.getHeadPitch() - entity.prevRotationPitch) * partialTicks;
            float f7 = event.getHeadPitch();
            renderLivingAt(entity, x, y, z);
            float f8 = handleRotationFloat(entity, partialTicks);
            applyRotations(entity, f8, f, partialTicks);
            float f4 = prepareScale(entity, partialTicks);
            float f5 = 0.0F;
            float f6 = 0.0F;

            if (!entity.isRiding())
            {
                f5 = entity.prevLimbSwingAmount + (entity.limbSwingAmount - entity.prevLimbSwingAmount) * partialTicks;
                f6 = entity.limbSwing - entity.limbSwingAmount * (1.0F - partialTicks);

                if (entity.isChild())
                {
                    f6 *= 3.0F;
                }

                if (f5 > 1.0F)
                {
                    f5 = 1.0F;
                }
                f2 = f1 - f; // Forge: Fix MC-1207
            }

            GlStateManager.enableAlpha();
            mainModel.setLivingAnimations(entity, f6, f5, partialTicks);
            mainModel.setRotationAngles(f6, f5, f8, f2, f7, f4, entity);

            if (renderOutlines)
            {
                boolean flag1 = setScoreTeamColor(entity);
                GlStateManager.enableColorMaterial();
                GlStateManager.enableOutlineMode(getTeamColor(entity));

                if (!renderMarker)
                {
                    renderModel(entity, f6, f5, f8, f2, f7, f4);
                }

                if (!(entity instanceof EntityPlayer) || !((EntityPlayer)entity).isSpectator())
                {
                    renderLayers(entity, f6, f5, partialTicks, f8, f2, f7, f4);
                }

                GlStateManager.disableOutlineMode();
                GlStateManager.disableColorMaterial();

                if (flag1)
                {
                    unsetScoreTeamColor();
                }
            }
            else
            {
                boolean flag = setDoRenderBrightness(entity, partialTicks);
                renderModel(entity, f6, f5, f8, f2, f7, f4);

                if (flag)
                {
                    unsetBrightness();
                }

                GlStateManager.depthMask(true);

                if (!(entity instanceof EntityPlayer) || !((EntityPlayer)entity).isSpectator())
                {
                    renderLayers(entity, f6, f5, partialTicks, f8, f2, f7, f4);
                }
            }

            GlStateManager.disableRescaleNormal();
        }
        catch (Exception exception)
        {
            LOGGER.error("Couldn't render entity", (Throwable)exception);
        }

        GlStateManager.setActiveTexture(OpenGlHelper.lightmapTexUnit);
        GlStateManager.enableTexture2D();
        GlStateManager.setActiveTexture(OpenGlHelper.defaultTexUnit);
        GlStateManager.enableCull();
        GlStateManager.popMatrix();
        super.doRender(entity, x, y, z, entityYaw, partialTicks);
    }
}
