package dev.tigr.ares.fabric.mixin.render;

import dev.tigr.ares.Wrapper;
import dev.tigr.ares.core.Ares;
import dev.tigr.ares.core.event.render.PlayerModelRenderEvent;
import dev.tigr.ares.fabric.event.render.RenderLivingEvent;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.PlayerEntityRenderer;
import net.minecraft.client.render.entity.feature.FeatureRenderer;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Iterator;
import java.util.List;

/**
 * @author Tigermouthbear 8/30/20
 * @author Makrennel 10/13/21 RenderModel Rotation
 */
@Mixin(LivingEntityRenderer.class)
public abstract class MixinLivingEntityRenderer<T extends LivingEntity, M extends EntityModel<T>> extends EntityRenderer<T> implements FeatureRendererContext<T, M>, Wrapper {
    @Shadow protected M model;

    @Shadow protected abstract float getHandSwingProgress(T entity, float tickDelta);

    @Shadow protected abstract float getAnimationProgress(T entity, float tickDelta);

    @Shadow protected abstract void setupTransforms(T entity, MatrixStack matrices, float animationProgress, float bodyYaw, float tickDelta);

    @Shadow protected abstract void scale(T entity, MatrixStack matrices, float amount);

    @Shadow protected abstract boolean isVisible(T entity);

    @Shadow protected @Nullable abstract RenderLayer getRenderLayer(T entity, boolean showBody, boolean translucent, boolean showOutline);

    @Shadow protected abstract float getAnimationCounter(T entity, float tickDelta);

    @Shadow @Final protected List<FeatureRenderer<T, M>> features;

    protected MixinLivingEntityRenderer(EntityRenderDispatcher dispatcher) {
        super(dispatcher);
    }

    @Inject(method = "render", at = @At("HEAD"))
    public void renderPre(T livingEntity, float f, float g, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i, CallbackInfo ci) {
        Ares.EVENT_MANAGER.post(new RenderLivingEvent.Pre(livingEntity));
    }

    @Inject(method = "render", at = @At("RETURN"))
    public void renderPost(T livingEntity, float f, float g, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i, CallbackInfo ci) {
        Ares.EVENT_MANAGER.post(new RenderLivingEvent.Post(livingEntity));
    }

    Float lastPitch = null;
    @Inject(method = "render(Lnet/minecraft/entity/LivingEntity;FFLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V", at = @At("HEAD"), cancellable = true)
    public void onRenderModel(T livingEntity, float f, float g, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i, CallbackInfo ci) {
        PlayerModelRenderEvent event = new PlayerModelRenderEvent(livingEntity.getEntityId(), livingEntity.pitch, livingEntity.headYaw, livingEntity.bodyYaw);
        if(!Ares.EVENT_MANAGER.post(event).isCancelled()) {
            return;
        }

        ci.cancel();

        matrixStack.push();
        this.model.handSwingProgress = this.getHandSwingProgress(livingEntity, g);
        this.model.riding = livingEntity.hasVehicle();
        this.model.child = livingEntity.isBaby();

        // Setting these variables directly looks uglier because Minecraft usually interpolates changes in angle which smoothens the animation
//        float h = event.getBodyYaw();
//        float j = event.getHeadYaw();
        livingEntity.headYaw = event.getHeadYaw();
        livingEntity.bodyYaw = event.getBodyYaw();
        float h = MathHelper.lerpAngleDegrees(g, livingEntity.prevBodyYaw, livingEntity.bodyYaw);
        float j = MathHelper.lerpAngleDegrees(g, livingEntity.prevHeadYaw, livingEntity.headYaw);

        float k = j - h;
        float o;
        if (livingEntity.hasVehicle() && livingEntity.getVehicle() instanceof LivingEntity) {
            LivingEntity livingEntity2 = (LivingEntity)livingEntity.getVehicle();
            h = MathHelper.lerpAngleDegrees(g, livingEntity2.prevBodyYaw, livingEntity2.bodyYaw);
            k = j - h;
            o = MathHelper.wrapDegrees(k);
            if (o < -85.0F) {
                o = -85.0F;
            }

            if (o >= 85.0F) {
                o = 85.0F;
            }

            h = j - o;
            if (o * o > 2500.0F) {
                h += o * 0.2F;
            }

            k = j - h;
        }

        // There is no built in setter in minecraft for headPitch (without just changing the pitch), but we can still interpolate it
        if(lastPitch == null) lastPitch = livingEntity.prevPitch;
        float m = MathHelper.lerp(g, lastPitch, event.getHeadPitch());
        lastPitch = event.getHeadPitch();
        livingEntity.prevPitch = event.getHeadPitch();

        float p;
        if (livingEntity.getPose() == EntityPose.SLEEPING) {
            Direction direction = livingEntity.getSleepingDirection();
            if (direction != null) {
                p = livingEntity.getEyeHeight(EntityPose.STANDING) - 0.1F;
                matrixStack.translate((double)((float)(-direction.getOffsetX()) * p), 0.0D, (double)((float)(-direction.getOffsetZ()) * p));
            }
        }

        o = this.getAnimationProgress(livingEntity, g);
        this.setupTransforms(livingEntity, matrixStack, o, h, g);
        matrixStack.scale(-1.0F, -1.0F, 1.0F);
        this.scale(livingEntity, matrixStack, g);
        matrixStack.translate(0.0D, -1.5010000467300415D, 0.0D);
        p = 0.0F;
        float q = 0.0F;
        if (!livingEntity.hasVehicle() && livingEntity.isAlive()) {
            p = MathHelper.lerp(g, livingEntity.lastLimbDistance, livingEntity.limbDistance);
            q = livingEntity.limbAngle - livingEntity.limbDistance * (1.0F - g);
            if (livingEntity.isBaby()) {
                q *= 3.0F;
            }

            if (p > 1.0F) {
                p = 1.0F;
            }
        }

        this.model.animateModel(livingEntity, q, p, g);
        this.model.setAngles(livingEntity, q, p, o, k, m);
        MinecraftClient minecraftClient = MinecraftClient.getInstance();
        boolean bl = this.isVisible(livingEntity);
        boolean bl2 = !bl && !livingEntity.isInvisibleTo(minecraftClient.player);
        boolean bl3 = minecraftClient.hasOutline(livingEntity);
        RenderLayer renderLayer = this.getRenderLayer(livingEntity, bl, bl2, bl3);
        if (renderLayer != null) {
            VertexConsumer vertexConsumer = vertexConsumerProvider.getBuffer(renderLayer);
            int r = PlayerEntityRenderer.getOverlay(livingEntity, this.getAnimationCounter(livingEntity, g));
            this.model.render(matrixStack, vertexConsumer, i, r, 1.0F, 1.0F, 1.0F, bl2 ? 0.15F : 1.0F);
        }

        if (!livingEntity.isSpectator()) {
            Iterator var23 = this.features.iterator();

            while(var23.hasNext()) {
                FeatureRenderer<T, M> featureRenderer = (FeatureRenderer)var23.next();
                featureRenderer.render(matrixStack, vertexConsumerProvider, i, livingEntity, q, p, g, o, k, m);
            }
        }

        matrixStack.pop();
        super.render(livingEntity, f, g, matrixStack, vertexConsumerProvider, i);
    }
}
