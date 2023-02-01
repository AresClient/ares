package org.aresclient.ares.mixins;

import net.minecraft.client.gl.VertexBuffer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(VertexBuffer.class)
public interface VertexBufferAccessor {
    @Accessor("vertexBufferId")
    int getVertexBufferId();

    @Accessor("indexBufferId")
    int getIndexBufferId();

    @Accessor("vertexArrayId")
    int getVertexArrayId();

    @Accessor("vertexBufferId")
    void setVertexBufferId(int value);

    @Accessor("indexBufferId")
    void setIndexBufferId(int value);

    @Accessor("vertexArrayId")
    void setVertexArrayId(int value);
}
