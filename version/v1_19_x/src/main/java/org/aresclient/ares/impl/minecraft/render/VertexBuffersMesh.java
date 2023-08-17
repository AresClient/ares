package org.aresclient.ares.impl.minecraft.render;

import net.minecraft.client.gl.VertexBuffer;
import org.aresclient.ares.api.minecraft.render.VertexBuffers;

public class VertexBuffersMesh implements VertexBuffers {
    @Override
    public void unbind() {
        VertexBuffer.unbind();
    }
}
