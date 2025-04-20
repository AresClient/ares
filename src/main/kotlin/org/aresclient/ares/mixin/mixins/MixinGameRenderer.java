package org.aresclient.ares.mixin.mixins;

import net.minecraft.client.render.Camera;
import net.minecraft.client.render.GameRenderer;
import org.aresclient.ares.api.JWrapper;
import org.aresclient.ares.api.events.RenderEvent;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public class MixinGameRenderer implements JWrapper {
    @Inject(method = "renderHand", at = @At("HEAD"))
    public void renderWorld(Camera camera, float tickProgress, Matrix4f positionMatrix, CallbackInfo ci) {
        EVENTS.post(new RenderEvent.World(tickProgress));
    }
}
