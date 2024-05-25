package org.aresclient.ares.impl.minecraft.render;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.render.GameRenderer;
import org.aresclient.ares.api.minecraft.AbstractMesh;
import org.aresclient.ares.api.minecraft.render.Camera;
import org.aresclient.ares.api.minecraft.render.Renderer;
import org.aresclient.ares.api.minecraft.render.VertexBuffers;
import org.joml.Matrix4f;

public class RendererMesh extends AbstractMesh<GameRenderer> implements Renderer {

    public RendererMesh(GameRenderer value) {
        super(value);
    }

    @Override
    public Camera getCamera() {
        return new CameraMesh(getMeshValue().getCamera());
    }

    @Override
    public Matrix4f getProjectionMatrix() {
        return RenderSystem.getProjectionMatrix();
    }

    @Override
    public Matrix4f getModelMatrix() {
        return RenderSystem.getModelViewMatrix();
    }

    private static final VertexBuffersMesh VERTEX_BUFFERS = new VertexBuffersMesh();
    @Override
    public VertexBuffers getVertexBuffers() {
        return VERTEX_BUFFERS;
    }
}
