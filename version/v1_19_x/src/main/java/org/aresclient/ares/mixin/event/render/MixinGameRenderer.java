package org.aresclient.ares.mixin.event.render;

import net.minecraft.client.render.Camera;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;
import org.aresclient.ares.api.Ares;
import org.aresclient.ares.api.event.render.RenderEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public class MixinGameRenderer {
    @Inject(method = "renderHand", at = @At("HEAD"))
    public void renderWorld(MatrixStack matrices, Camera camera, float tickDelta, CallbackInfo ci) {
        Ares.getEventManager().post(new RenderEvent.World(tickDelta));
    }
}
