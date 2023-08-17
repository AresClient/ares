package org.aresclient.ares.api.minecraft.render;

import org.joml.Matrix4f;

public interface Renderer {
    Camera getCamera();

    Matrix4f getProjectionMatrix();

    Matrix4f getModelMatrix();

    VertexBuffers getVertexBuffers();
}
