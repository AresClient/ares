package org.aresclient.ares.mixininterface;

public interface IVertexBuffer {
    int getVertexBufferId();

    int getIndexBufferId();

    int getVertexArrayId();

    void setVertexBufferId(int value);

    void setIndexBufferId(int value);

    void setVertexArrayId(int value);
}
