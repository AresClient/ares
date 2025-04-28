package org.aresclient.ares.mixin.mixins;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;
import org.aresclient.ares.api.JWrapper;
import org.aresclient.ares.api.events.RenderEvent;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public abstract class MixinGameRenderer implements JWrapper {
    @Unique private final MatrixStack matrices = new MatrixStack();

    @Shadow protected abstract void bobView(MatrixStack matrices, float tickProgress);

    @Shadow @Final private MinecraftClient client;

    @Shadow protected abstract void tiltViewWhenHurt(MatrixStack matrices, float tickProgress);

    @Inject(method = "renderHand", at = @At("HEAD"))
    public void renderWorld(Camera camera, float tickProgress, Matrix4f positionMatrix, CallbackInfo ci) {
        matrices.push();
        tiltViewWhenHurt(matrices, tickProgress);
        if(client.options.getBobView().getValue()) bobView(matrices, tickProgress);
        EVENTS.post(new RenderEvent.World(tickProgress, matrices.peek().getPositionMatrix()));
        matrices.pop();
    }
}
