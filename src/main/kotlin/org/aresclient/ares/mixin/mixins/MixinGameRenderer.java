package org.aresclient.ares.mixin.mixins;

import net.minecraft.client.render.Camera;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.util.math.MatrixStack;
import org.aresclient.ares.api.JWrapper;
import org.aresclient.ares.api.events.RenderEvent;
import org.aresclient.ares.impl.instrument.modules.render.NoRender;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public abstract class MixinGameRenderer implements JWrapper {
    @Inject(method = "renderHand", at = @At("HEAD"))
    private void renderWorld(Camera camera, float tickProgress, Matrix4f positionMatrix, CallbackInfo ci) {
        EVENTS.post(new RenderEvent.World(tickProgress));
    }

    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/profiler/Profiler;pop()V"))
    private void render(RenderTickCounter tickCounter, boolean tick, CallbackInfo ci) {
        EVENTS.post(new RenderEvent.Hud(tickCounter.getFixedDeltaTicks()));
    }

    @Redirect(method = "renderWorld", at = @At(value = "INVOKE", target = "Ljava/lang/Double;floatValue()F"))
    private float renderWorld(Double instance) {
        if(NoRender.INSTANCE.shouldBlockNausea()) return 0f;
        return instance.floatValue();
    }

    @Inject(method = "tiltViewWhenHurt", at = @At("HEAD"), cancellable = true)
    private void tiltViewWhenHurt(MatrixStack matrices, float tickProgress, CallbackInfo ci) {
        if(NoRender.INSTANCE.shouldBlockHurtShake()) ci.cancel();
    }
}
