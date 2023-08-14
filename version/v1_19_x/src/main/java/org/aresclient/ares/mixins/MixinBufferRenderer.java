package org.aresclient.ares.mixins;

import net.minecraft.client.gl.VertexBuffer;
import net.minecraft.client.render.BufferRenderer;
import org.aresclient.ares.mixininterface.IBufferRenderer;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(BufferRenderer.class)
public class MixinBufferRenderer implements IBufferRenderer {
    @Shadow @Nullable private static VertexBuffer currentVertexBuffer;

    @Override
    public VertexBuffer getCurrentVertexBuffer() {
        return currentVertexBuffer;
    }
}
