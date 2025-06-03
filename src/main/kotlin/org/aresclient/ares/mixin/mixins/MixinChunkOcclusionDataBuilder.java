package org.aresclient.ares.mixin.mixins;

import net.minecraft.client.render.chunk.ChunkOcclusionDataBuilder;
import org.aresclient.ares.api.JWrapper;
import org.aresclient.ares.api.events.BlockOcclusionEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ChunkOcclusionDataBuilder.class)
public class MixinChunkOcclusionDataBuilder implements JWrapper {
    @Inject(method = "markClosed", at = @At("HEAD"), cancellable = true)
    public void markClosed(CallbackInfo ci) {
        if(EVENTS.post(new BlockOcclusionEvent()).isCancelled()) ci.cancel();
    }
}
