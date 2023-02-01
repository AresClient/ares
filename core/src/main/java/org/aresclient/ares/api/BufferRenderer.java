package org.aresclient.ares.api;

// 1.13+?, 1.12 uses legacy opengl rendering
public interface BufferRenderer {
    int getVertexArray();
    void setVertexArray(int vao);

    int getVertexBuffer();
    void setVertexBuffer(int vbo);

    int getElementBuffer();
    void setElementBuffer(int ibo);
}
