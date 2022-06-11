package org.aresclient.ares.renderer;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public class VertexFormat {
    public static final VertexFormat POSITION = new Builder()
            .add(VertexTypes.FLOAT, 3) // position
            .build();

    public static final VertexFormat POSITION_COLOR = new Builder()
            .add(VertexTypes.FLOAT, 3) // position
            .add(VertexTypes.FLOAT, 4) // color
            .build();

    public static final VertexFormat POSITION_UV = new Builder()
            .add(VertexTypes.FLOAT, 3) // position
            .add(VertexTypes.FLOAT, 2) // uv
            .build();

    public static final VertexFormat POSITION_UV_COLOR = new Builder()
            .add(VertexTypes.FLOAT, 3) // position
            .add(VertexTypes.FLOAT, 2) // uv
            .add(VertexTypes.FLOAT, 4) // color
            .build();

    public static final VertexFormat LINES = new Builder()
            .add(VertexTypes.FLOAT, 4) // x, y, z, width
            .add(VertexTypes.FLOAT, 4) // color
            .build();

    private static class Vertex {
        final VertexTypes type;
        final int len;

        Vertex(VertexTypes type, int len) {
            this.type = type;
            this.len = len;
        }
    }

    public enum VertexTypes {
        BYTE(1, GL11.GL_BYTE),
        FLOAT(4, GL11.GL_FLOAT),
        INT(4, GL11.GL_INT),
        SHORT(2, GL11.GL_SHORT);

        private final int size;
        private final int gl;

        VertexTypes(int size, int gl) {
            this.size = size;
            this.gl = gl;
        }
    }

    private final int stride;
    private final Vertex[] vertices;

    private VertexFormat(int stride, Vertex[] vertices) {
        this.stride = stride;
        this.vertices = vertices;
    }

    void use() {
        int i = 0;
        long p = 0;
        for(Vertex vertex: vertices) {
            GL20.glVertexAttribPointer(i, vertex.len, vertex.type.gl,  false, stride, p);
            GL20.glEnableVertexAttribArray(i++);
            p += (long) vertex.len * vertex.type.size;
        }
    }

    public BufferBuilder buffer() {
        return new BufferBuilder(this);
    }

    public static class Builder {
        private int stride = 0;
        private final ArrayList<Vertex> vertices = new ArrayList<>();

        public Builder add(VertexTypes type, int size) {
            vertices.add(new Vertex(type, size));
            stride += size * type.size;
            return this;
        }

        public Builder add(VertexTypes type) {
            return add(type, 1);
        }

        public VertexFormat build() {
            Vertex[] out = new Vertex[vertices.size()];
            for(int i = 0; i < vertices.size(); i++) out[i] = vertices.get(i);
            return new VertexFormat(stride, out);
        }
    }

    public static class BufferBuilder {
        private final List<Number[]> data = new ArrayList<>();
        private final VertexFormat vertexFormat;

        private BufferBuilder(VertexFormat vertexFormat) {
            this.vertexFormat = vertexFormat;
        }

        public BufferBuilder bytes(byte... bytes) {
            Number[] nums = new Number[bytes.length];
            for(int i = 0; i < bytes.length; i++) nums[i] = bytes[i];
            data.add(nums);
            return this;
        }

        public BufferBuilder floats(float... floats) {
            Number[] nums = new Number[floats.length];
            for(int i = 0; i < floats.length; i++) nums[i] = floats[i];
            data.add(nums);
            return this;
        }

        public BufferBuilder ints(int... ints) {
            Number[] nums = new Number[ints.length];
            for(int i = 0; i < ints.length; i++) nums[i] = ints[i];
            data.add(nums);
            return this;
        }

        public BufferBuilder shorts(short... shorts) {
            Number[] nums = new Number[shorts.length];
            for(int i = 0; i < shorts.length; i++) nums[i] = shorts[i];
            data.add(nums);
            return this;
        }

        public ByteBuffer build() {
            int len = data.get(0).length / vertexFormat.vertices[0].len;
            ByteBuffer buffer = BufferUtils.createByteBuffer(len * vertexFormat.stride);
            for(int i = 0; i < len; i++) {
                for(int j = 0; j < vertexFormat.vertices.length; j++) {
                    Vertex vert = vertexFormat.vertices[j];
                    for(int k = 0; k < vert.len; k++) {
                        Number num = data.get(j)[k + (i * vert.len)];

                        if(vert.type == VertexTypes.BYTE) buffer.put(num.byteValue());
                        else if(vert.type == VertexTypes.FLOAT) buffer.putFloat(num.floatValue());
                        else if(vert.type == VertexTypes.INT) buffer.putInt(num.intValue());
                        else if(vert.type == VertexTypes.SHORT) buffer.putShort(num.shortValue());
                    }
                }
            }
            return (ByteBuffer) buffer.flip();
        }
    }
}
