package org.aresclient.ares.mixin.accessors;

import net.minecraft.client.render.BufferBuilder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(BufferBuilder.class)
public interface AccessBufferBuilder {
    @Accessor("vertexCount")
    int getVertexCount();
}
