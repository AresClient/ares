package org.aresclient.ares.api.render;

import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL32;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class Shader {
    private static final List<Shader> SHADERS = new ArrayList<>();

    public static final Shader POSITION_COLOR = fromResources("/assets/ares/shaders/vert/position_color.vert", "/assets/ares/shaders/frag/position_color.frag");
    public static final Shader POSITION_TEXTURE = fromResources("/assets/ares/shaders/vert/position_texture.vert", "/assets/ares/shaders/frag/position_texture.frag");
    public static final Shader POSITION_TEXTURE_COLOR = fromResources("/assets/ares/shaders/vert/position_texture_color.vert", "/assets/ares/shaders/frag/position_texture_color.frag");
    public static final Shader LINES = fromResources("/assets/ares/shaders/vert/lines.vert", "/assets/ares/shaders/frag/lines.frag", "/assets/ares/shaders/geom/lines.geom");
    public static final Shader ELLIPSE = fromResources("/assets/ares/shaders/vert/ellipse.vert", "/assets/ares/shaders/frag/ellipse.frag");
    public static final Shader ROUNDED = fromResources("/assets/ares/shaders/vert/ellipse.vert", "/assets/ares/shaders/frag/round.frag");

    private final Map<String, List<Uniform>> uniforms = new HashMap<>();
    private final int program = GL20.glCreateProgram();

    private boolean attached = false;

    public static Shader fromResources(String vertPath, String fragPath) {
        return new Shader(Shader.class.getResourceAsStream(vertPath), Shader.class.getResourceAsStream(fragPath));
    }

    public static Shader fromResources(String vertPath, String fragPath, String geomPath) {
        return new Shader(Shader.class.getResourceAsStream(vertPath), Shader.class.getResourceAsStream(fragPath), Shader.class.getResourceAsStream(geomPath));
    }


    public Shader(InputStream vertIs, InputStream fragIs) {
        this(
            (new BufferedReader(new InputStreamReader(vertIs))).lines().collect(Collectors.joining("\n")),
            (new BufferedReader(new InputStreamReader(fragIs))).lines().collect(Collectors.joining("\n"))
        );
    }

    public Shader(InputStream vertIs, InputStream fragIs, InputStream geomIs) {
        this(
            (new BufferedReader(new InputStreamReader(vertIs))).lines().collect(Collectors.joining("\n")),
            (new BufferedReader(new InputStreamReader(fragIs))).lines().collect(Collectors.joining("\n")),
            (new BufferedReader(new InputStreamReader(geomIs))).lines().collect(Collectors.joining("\n"))
        );
    }

    public Shader(String vert, String frag) {
        this(vert, frag, null);
    }

    public Shader(String vert, String frag, String geom) {
        add(vert, GL20.GL_VERTEX_SHADER);
        add(frag, GL20.GL_FRAGMENT_SHADER);
        if(geom != null) add(geom, GL32.GL_GEOMETRY_SHADER);

        GL20.glLinkProgram(program);
        if(GL20.glGetProgrami(program, GL20.GL_LINK_STATUS) == 0) {
            throw new RuntimeException("Failed to link program");
        }

        GL20.glValidateProgram(program);
        if(GL20.glGetProgrami(program, GL20.GL_VALIDATE_STATUS) == 0) {
            throw new RuntimeException("Failed to validate program");
        }

        SHADERS.add(this);
    }

    private void add(String source, int type) {
        int shader = GL20.glCreateShader(type);
        GL20.glShaderSource(shader, source);
        GL20.glCompileShader(shader);
        GL20.glAttachShader(program, shader);
    }

    int getProgram() {
        return program;
    }

    public void attach() {
        GL20.glUseProgram(program);
        attached = true;
    }

    public void detach() {
        GL20.glUseProgram(0);
        attached = false;
    }

    public boolean isAttached() {
        return attached;
    }

    public void delete() {
        GL20.glDeleteProgram(program);
        SHADERS.remove(this);
    }

    public static void clear() {
        for(Shader shader: SHADERS) GL20.glDeleteProgram(shader.program);
        SHADERS.clear();
    }

    private <T extends Uniform> T add(T uniform) {
        uniforms.computeIfAbsent(uniform.getName(), name -> new ArrayList<>()).add(uniform);
        return uniform;
    }

    public Uniform.F1 uniformF1(String name) {
        return add(new Uniform.F1(name, this));
    }

    public Uniform.I1 uniformI1(String name) {
        return add(new Uniform.I1(name, this));
    }

    public Uniform.F2 uniformF2(String name) {
        return add(new Uniform.F2(name, this));
    }

    public Uniform.I2 uniformI2(String name) {
        return add(new Uniform.I2(name, this));
    }

    public Uniform.F3 uniformF3(String name) {
        return add(new Uniform.F3(name, this));
    }

    public Uniform.I3 uniformI3(String name) {
        return add(new Uniform.I3(name, this));
    }

    public Uniform.F4 uniformF4(String name) {
        return add(new Uniform.F4(name, this));
    }

    public Uniform.I4 uniformI4(String name) {
        return add(new Uniform.I4(name, this));
    }

    public Uniform.Mat2f uniformMat2f(String name) {
        return add(new Uniform.Mat2f(name, this));
    }

    public Uniform.Mat3f uniformMat3f(String name) {
        return add(new Uniform.Mat3f(name, this));
    }

    public Uniform.Mat3x2f uniformMat3x2f(String name) {
        return add(new Uniform.Mat3x2f(name, this));
    }

    public Uniform.Mat4f uniformMat4f(String name) {
        return add(new Uniform.Mat4f(name, this));
    }

    public Uniform.Mat4x3f uniformMat4x3f(String name) {
        return add(new Uniform.Mat4x3f(name, this));
    }

    void markDirty(Uniform uniform) {
        List<Uniform> list = uniforms.get(uniform.getName());
        if(list != null) list.forEach(Uniform::markDirty);
    }
}
