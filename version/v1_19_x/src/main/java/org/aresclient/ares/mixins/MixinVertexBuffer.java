package org.aresclient.ares.mixins;

import net.minecraft.client.gl.VertexBuffer;
import org.aresclient.ares.mixininterface.IVertexBuffer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(VertexBuffer.class)
public class MixinVertexBuffer implements IVertexBuffer {
    @Shadow private int vertexBufferId;

    @Shadow private int indexBufferId;

    @Shadow private int vertexArrayId;

    @Override
    public int getVertexBufferId() {
        return vertexBufferId;
    }

    @Override
    public int getIndexBufferId() {
        return indexBufferId;
    }

    @Override
    public int getVertexArrayId() {
        return vertexArrayId;
    }

    @Override
    public void setVertexBufferId(int value) {
        vertexBufferId = value;
    }

    @Override
    public void setIndexBufferId(int value) {
        indexBufferId = value;
    }

    @Override
    public void setVertexArrayId(int value) {
        vertexArrayId = value;
    }
}
