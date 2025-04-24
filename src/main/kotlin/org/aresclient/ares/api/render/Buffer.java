package org.aresclient.ares.api.render;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;

public class Buffer {
    private final static List<Buffer> BUFFERS = new ArrayList<>();

    private final int vao = GL30.glGenVertexArrays();
    private final int vbo = GL15.glGenBuffers();
    private final int ibo = GL15.glGenBuffers();

    private final Shader shader;
    private final VertexFormat vertexFormat;

    private final List<Uniform> uniforms = new ArrayList<>();
    private final boolean dynamic;

    private boolean building = true;
    private ByteBuffer vertBuffer;
    private IntBuffer indexBuffer;
    private int vertexSize, indexSize; // vertexSize is size in bytes and indexSize is size in ints (4 bytes)
    private int vertexPos = 0, indexPos = 0;
    private boolean vertexDirty = false, indexDirty = false;
    private boolean vertexSizeDirty = true, indexSizeDirty = true;

    private boolean lines = false;
    private Uniform.F2 linesViewport = null;
    private final int projection;
    private final int model;

    public static Buffer createStatic(Shader shader, VertexFormat vertexFormat, int vert, int index) {
        return new Buffer(shader, vertexFormat, false, vert, index);
    }

    public static Buffer createDynamic(Shader shader, VertexFormat vertexFormat) {
        return new Buffer(shader, vertexFormat, true, 0, 0);
    }

    public Buffer lines(float aa) {
        lines = true;
        linesViewport = shader.uniformF2("u_viewport_size");
        uniform(linesViewport);
        uniform(shader.uniformF2("u_aa_radius").set(aa, aa));
        return this;
    }

    public Buffer lines() {
        return lines(2);
    }

    private Buffer(Shader shader, VertexFormat vertexFormat, boolean dynamic, int vert, int index) {
        this.dynamic = dynamic;
        if(dynamic) {
            vert = 4;
            index = 6;
        }
        this.shader = shader;
        this.vertexFormat = vertexFormat;
        this.vertexSize = vert * vertexFormat.getStride();
        this.indexSize = index;
        this.vertBuffer = BufferUtils.createByteBuffer(vertexSize);
        this.indexBuffer = BufferUtils.createIntBuffer(indexSize);
        this.projection = GL20.glGetUniformLocation(shader.getProgram(), "projection");
        this.model = GL20.glGetUniformLocation(shader.getProgram(), "model");

        GL30.glBindVertexArray(vao);
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vbo);

        vertexFormat.use();

        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
        GL30.glBindVertexArray(0);

        BUFFERS.add(this);
    }

    public Buffer reset() {
        return resetVertices().resetIndices();
    }

    public Buffer resetVertices() {
        vertexPos = 0;
        vertBuffer.clear();
        building = true;
        return this;
    }

    public Buffer resetIndices() {
        indexPos = 0;
        indexBuffer.clear();
        building = true;
        return this;
    }

    public Buffer vertices(float... data) {
        vertexPos += data.length * 4;
        tryIncreaseVertexBuffer();
        for(float f: data) vertBuffer.putFloat(f);
        vertexDirty = true;
        return this;
    }

    public Buffer vertices(ByteBuffer buffer) {
        vertexPos += buffer.remaining();
        tryIncreaseVertexBuffer();
        vertBuffer.put(buffer);
        vertexDirty = true;
        return this;
    }

    private void tryIncreaseVertexBuffer() {
        if(vertexPos > vertexSize) {
            int size = vertexSize;
            while(size < vertexPos) size *= 2;

            ByteBuffer buffer = BufferUtils.createByteBuffer(size);
            buffer.put(vertBuffer.flip());

            vertBuffer = buffer;
            vertexSize = size;
            vertexSizeDirty = true;
        }
    }

    public Buffer indices(int... data) {
        indexPos += data.length;
        tryIncreaseIndexBuffer();
        indexBuffer.put(data);
        indexDirty = true;
        return this;
    }

    public Buffer indicesOffset(int... data) {
        int offset = vertexPos / vertexFormat.getStride();
        int[] out = new int[data.length];
        for(int i = 0; i < data.length; i++) out[i] = data[i] + offset;
        return indices(out);
    }

    private void tryIncreaseIndexBuffer() {
        if(indexPos > indexSize) {
            int size = indexSize;
            while(size < indexPos) size *= 2;

            IntBuffer buffer = BufferUtils.createIntBuffer(size);
            buffer.put(indexBuffer.flip());

            indexBuffer = buffer;
            indexSize = size;
            indexSizeDirty = true;
        }
    }

    private void end() {
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vbo);
        GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, ibo);

        if(vertexSizeDirty) {
            vertBuffer.flip().limit(vertexSize);
            GL15.glBufferData(GL15.GL_ARRAY_BUFFER, vertBuffer, dynamic ? GL15.GL_DYNAMIC_DRAW : GL15.GL_STATIC_DRAW);
            vertexSizeDirty = false;
            vertexDirty = false;
        } else if(vertexDirty) {
            GL15.glBufferSubData(GL15.GL_ARRAY_BUFFER, 0, vertBuffer.flip());
            vertexDirty = false;
        }

        if(indexSizeDirty) {
            indexBuffer.flip().limit(indexSize);
            GL15.glBufferData(GL15.GL_ELEMENT_ARRAY_BUFFER, indexBuffer, dynamic ? GL15.GL_DYNAMIC_DRAW : GL15.GL_STATIC_DRAW);
            indexSizeDirty = false;
            indexDirty = false;
        } else if(indexDirty) {
            GL15.glBufferSubData(GL15.GL_ELEMENT_ARRAY_BUFFER, 0, indexBuffer.flip());
            indexDirty = false;
        }

        GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, 0);
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
    }

    public void draw() {
        draw(MatrixStack.EMPTY);
    }

    public void draw(MatrixStack matrixStack) {
        if(building) {
            end();
            building = false;
        }

        if(shader != null && !shader.isAttached()) shader.attach();

        if(lines) {
            int[] viewport = new int[4];
            GL11.glGetIntegerv(GL30.GL_VIEWPORT, viewport);
            linesViewport.set(viewport[2], viewport[3]);
        }

        matrixStack.update(projection, model);
        for(Uniform uniform: uniforms) uniform.update();

        GL30.glBindVertexArray(vao);
        GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, ibo);
        GL11.glDrawElements(lines ? GL11.GL_LINES : GL11.GL_TRIANGLES, indexPos, GL11.GL_UNSIGNED_INT, 0);
        GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, 0);
        GL30.glBindVertexArray(0);

        if(shader != null) shader.detach();
    }

    public boolean shouldRender() {
        return indexPos > 0;
    }

    public Buffer uniform(Uniform uniform) {
        uniforms.add(uniform);
        return this;
    }

    public void delete() {
        GL30.glDeleteVertexArrays(vao);
        GL15.glDeleteBuffers(vbo);
        GL15.glDeleteBuffers(ibo);
        BUFFERS.remove(this);
    }

    public static void clear() {
        for(Buffer buffer: BUFFERS) {
            GL30.glDeleteVertexArrays(buffer.vao);
            GL15.glDeleteBuffers(buffer.vbo);
            GL15.glDeleteBuffers(buffer.ibo);
        }
        BUFFERS.clear();
    }
}
