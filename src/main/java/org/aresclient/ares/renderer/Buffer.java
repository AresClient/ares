package org.aresclient.ares.renderer;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;

public class Buffer {
    private final static List<Buffer> BUFFERS = new ArrayList<>();

    private final int vao = GL30.glGenVertexArrays();
    private final int vbo = GL15.glGenBuffers();
    private final int ibo = GL15.glGenBuffers();


    private final Shader shader;

    private final List<Uniform> uniforms = new ArrayList<>();
    private final boolean dynamic;
    private boolean vertices = false;
    private boolean indices = false;

    private boolean lines = false;
    private Uniform.F2 linesViewport = null;
    private final int projection;
    private final int model;

    private int indexCount = 0;

    public static Buffer beginStatic(Shader shader, VertexFormat vertexFormat) {
        return new Buffer(shader, vertexFormat, false);
    }

    public static Buffer beginDynamic(Shader shader, VertexFormat vertexFormat) {
        return new Buffer(shader, vertexFormat, true);
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

    private Buffer(Shader shader, VertexFormat vertexFormat, boolean dynamic) {
        this.dynamic = dynamic;
        this.shader = shader;
        this.projection = GL20.glGetUniformLocation(shader.getProgram(), "projection");
        this.model = GL20.glGetUniformLocation(shader.getProgram(), "model");

        GL30.glBindVertexArray(vao);
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vbo);
        GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, ibo);

        vertexFormat.use();

        BUFFERS.add(this);
    }

    public Buffer vertices(float... data) {
        FloatBuffer buffer = (FloatBuffer) BufferUtils.createFloatBuffer(data.length).put(data).flip();

        if(vertices) GL15.glBufferSubData(GL15.GL_ARRAY_BUFFER, 0, buffer);
        else GL15.glBufferData(GL15.GL_ARRAY_BUFFER, buffer, dynamic ? GL15.GL_DYNAMIC_DRAW : GL15.GL_STATIC_DRAW);

        vertices = true;
        return this;
    }

    public Buffer vertices(ByteBuffer buffer) {
        if(vertices) GL15.glBufferSubData(GL15.GL_ARRAY_BUFFER, 0, buffer);
        else GL15.glBufferData(GL15.GL_ARRAY_BUFFER, buffer, dynamic ? GL15.GL_DYNAMIC_DRAW : GL15.GL_STATIC_DRAW);

        vertices = true;
        return this;
    }

    public Buffer indices(int... data) {
        IntBuffer buffer = (IntBuffer) BufferUtils.createIntBuffer(data.length).put(data).flip();

        if(indices) GL15.glBufferSubData(GL15.GL_ELEMENT_ARRAY_BUFFER, 0, buffer);
        else GL15.glBufferData(GL15.GL_ELEMENT_ARRAY_BUFFER, buffer, dynamic ? GL15.GL_DYNAMIC_DRAW : GL15.GL_STATIC_DRAW);

        indices = true;
        indexCount = data.length;
        return this;
    }

    public Buffer end() {
        if(!vertices) throw new RuntimeException("Vertices not specified for buffer!");
        if(!indices) throw new RuntimeException("Indices not specified for buffer!");

        GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, 0);
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
        GL30.glBindVertexArray(0);
        return this;
    }

    public void draw() {
        draw(MatrixStack.EMPTY);
    }

    public void draw(MatrixStack matrixStack) {
        if(shader != null && !shader.isAttached()) shader.attach();

        if(lines) {
            int[] viewport = getViewport();
            linesViewport.set(viewport[2], viewport[3]);
        }

        matrixStack.update(projection, model);
        for(Uniform uniform: uniforms) uniform.update();

        GL30.glBindVertexArray(vao);
        GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, ibo);
        GL11.glDrawElements(lines ? GL11.GL_LINES : GL11.GL_TRIANGLES, indexCount, GL11.GL_UNSIGNED_INT, 0);
        GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, 0);
        GL30.glBindVertexArray(0);

        if(shader != null) shader.detach();
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


    // hacky code from now on...
    // why does 1.12.2 use lwjgl nightly????
    private static Method GL_GET_INTEGER_V = null;
    private static Method GL_GET_INTEGER = null;

    private static final IntBuffer GET_INT_BUFFER = BufferUtils.createIntBuffer(16);
    private static boolean GOT_METHOD = false;
    static int[] getViewport() {
        int[] out = new int[4];

        if(!GOT_METHOD) {
            try {
                GL_GET_INTEGER = GL30.class.getDeclaredMethod("glGetInteger", int.class, int.class, IntBuffer.class);
            } catch(NoSuchMethodException ignored) {
            }

            try {
                GL_GET_INTEGER_V = GL11.class.getDeclaredMethod("glGetIntegerv", int.class, int[].class);
            } catch(NoSuchMethodException ignored) {
            }

            GOT_METHOD = GL_GET_INTEGER != null || GL_GET_INTEGER_V != null;
            if(!GOT_METHOD) throw new RuntimeException("Failed to find glGetInteger method!");
        }

        if(GL_GET_INTEGER_V != null) {
            try {
                GL_GET_INTEGER_V.invoke(null, GL11.GL_VIEWPORT, out);
            } catch(IllegalAccessException | InvocationTargetException e) {
                throw new RuntimeException(e);
            }
        }

        if(GL_GET_INTEGER != null) {
            try {
                GL_GET_INTEGER.invoke(null, GL11.GL_VIEWPORT, 0, GET_INT_BUFFER);
            } catch(InvocationTargetException | IllegalAccessException e) {
                throw new RuntimeException(e);
            }
            out[0] = GET_INT_BUFFER.get(0);
            out[1] = GET_INT_BUFFER.get(1);
            out[2] = GET_INT_BUFFER.get(2);
            out[3] = GET_INT_BUFFER.get(3);
        }

        return out;
    }
}
