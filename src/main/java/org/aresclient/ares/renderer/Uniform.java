package org.aresclient.ares.renderer;

import org.joml.*;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL21;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.FloatBuffer;

public abstract class Uniform {
    private static boolean LEGACY;
    static {
        try {
            GL20.class.getDeclaredMethod("glUniformMatrix4fv", int.class, boolean.class, float[].class);
            LEGACY = false;
        } catch(NoSuchMethodException e) {
            LEGACY = true;
        }
    }

    public static class F1 extends Uniform {
        private float value;

        F1(int id) {
            super(id);
        }

        public F1 set(float value) {
            this.value = value;
            dirty = true;
            return this;
        }

        public float get() {
            return value;
        }

        @Override
        protected void uniform() {
            GL20.glUniform1f(id, value);
        }
    }

    public static class I1 extends Uniform {
        private int value;

        I1(int id) {
            super(id);
        }

        public I1 set(int value) {
            this.value = value;
            dirty = true;
            return this;
        }

        public int get() {
            return value;
        }

        @Override
        protected void uniform() {
            GL20.glUniform1i(id, value);
        }
    }

    public static class F2 extends Uniform {
        private final float[] value = new float[2];

        F2(int id) {
            super(id);
        }

        public F2 set(float value0, float value1) {
            value[0] = value0;
            value[1] = value1;
            dirty = true;
            return this;
        }

        public F2 set(float... value) {
            this.value[0] = value[0];
            this.value[1] = value[1];
            dirty = true;
            return this;
        }

        public float[] get() {
            return value;
        }

        @Override
        protected void uniform() {
            GL20.glUniform2f(id, value[0], value[1]);
        }
    }

    public static class I2 extends Uniform {
        private final int[] value = new int[2];

        I2(int id) {
            super(id);
        }

        public I2 set(int value0, int value1) {
            value[0] = value0;
            value[1] = value1;
            dirty = true;
            return this;
        }

        public I2 set(int... value) {
            this.value[0] = value[0];
            this.value[1] = value[1];
            dirty = true;
            return this;
        }

        public int[] get() {
            return value;
        }

        @Override
        protected void uniform() {
            GL20.glUniform2i(id, value[0], value[1]);
        }
    }

    public static class F3 extends Uniform {
        private final float[] value = new float[3];

        F3(int id) {
            super(id);
        }

        public F3 set(float value0, float value1, float value2) {
            value[0] = value0;
            value[1] = value1;
            value[2] = value2;
            dirty = true;
            return this;
        }

        public F3 set(float... value) {
            this.value[0] = value[0];
            this.value[1] = value[1];
            this.value[2] = value[2];
            dirty = true;
            return this;
        }

        public float[] get() {
            return value;
        }

        @Override
        protected void uniform() {
            GL20.glUniform3f(id, value[0], value[1], value[2]);
        }
    }

    public static class I3 extends Uniform {
        private final int[] value = new int[3];

        I3(int id) {
            super(id);
        }

        public I3 set(int value0, int value1, int value2) {
            value[0] = value0;
            value[1] = value1;
            value[2] = value2;
            dirty = true;
            return this;
        }

        public I3 set(int... value) {
            this.value[0] = value[0];
            this.value[1] = value[1];
            this.value[2] = value[2];
            dirty = true;
            return this;
        }

        public int[] get() {
            return value;
        }

        @Override
        protected void uniform() {
            GL20.glUniform3i(id, value[0], value[1], value[2]);
        }
    }

    public static class F4 extends Uniform {
        private final float[] value = new float[4];

        F4(int id) {
            super(id);
        }

        public F4 set(float value0, float value1, float value2, float value3) {
            value[0] = value0;
            value[1] = value1;
            value[2] = value2;
            value[3] = value3;
            dirty = true;
            return this;
        }

        public F4 set(float... value) {
            this.value[0] = value[0];
            this.value[1] = value[1];
            this.value[2] = value[2];
            this.value[3] = value[3];
            dirty = true;
            return this;
        }

        public float[] get() {
            return value;
        }

        @Override
        protected void uniform() {
            GL20.glUniform4f(id, value[0], value[1], value[2], value[3]);
        }
    }

    public static class I4 extends Uniform {
        private final int[] value = new int[4];

        I4(int id) {
            super(id);
        }

        public I4 set(int value0, int value1, int value2, int value3) {
            value[0] = value0;
            value[1] = value1;
            value[2] = value2;
            value[3] = value3;
            dirty = true;
            return this;
        }

        public I4 set(int... value) {
            this.value[0] = value[0];
            this.value[1] = value[1];
            this.value[2] = value[2];
            this.value[3] = value[3];
            dirty = true;
            return this;
        }

        public int[] get() {
            return value;
        }

        @Override
        protected void uniform() {
            GL20.glUniform4i(id, value[0], value[1], value[2], value[3]);
        }
    }

    public static class Mat2f extends Uniform {
        private static Method LEGACY_METHOD;
        static {
            try {
                LEGACY_METHOD = GL20.class.getMethod("glUniformMatrix2", int.class, boolean.class, FloatBuffer.class);
            } catch(NoSuchMethodException e) {
                if(LEGACY) throw new RuntimeException(e);
            }
        }

        private Matrix2f value;

        Mat2f(int id) {
            super(id);
        }

        public Mat2f set(Matrix2f value) {
            this.value = value;
            dirty = true;
            return this;
        }

        public Matrix2f get() {
            return value;
        }

        @Override
        protected void uniform() {
            FloatBuffer buffer = BufferUtils.createFloatBuffer(4);
            if(!LEGACY) GL20.glUniformMatrix2fv(id, false, value.get(buffer));
            else {
                try {
                    LEGACY_METHOD.invoke(null, id, false, value.get(buffer));
                } catch(IllegalAccessException | InvocationTargetException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    public static class Mat3f extends Uniform {
        private static Method LEGACY_METHOD;
        static {
            try {
                LEGACY_METHOD = GL20.class.getMethod("glUniformMatrix3", int.class, boolean.class, FloatBuffer.class);
            } catch(NoSuchMethodException e) {
                if(LEGACY) throw new RuntimeException(e);
            }
        }

        private Matrix3f value;

        Mat3f(int id) {
            super(id);
        }

        public Mat3f set(Matrix3f value) {
            this.value = value;
            dirty = true;
            return this;
        }

        public Matrix3f get() {
            return value;
        }

        @Override
        protected void uniform() {
            FloatBuffer buffer = BufferUtils.createFloatBuffer(9);
            if(!LEGACY) GL20.glUniformMatrix3fv(id, false, value.get(buffer));
            else {
                try {
                    LEGACY_METHOD.invoke(null, id, false, value.get(buffer));
                } catch(IllegalAccessException | InvocationTargetException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    public static class Mat3x2f extends Uniform {
        private static Method LEGACY_METHOD;
        static {
            try {
                LEGACY_METHOD = GL21.class.getMethod("glUniformMatrix3x2", int.class, boolean.class, FloatBuffer.class);
            } catch(NoSuchMethodException e) {
                if(LEGACY) throw new RuntimeException(e);
            }
        }

        private Matrix3x2f value;

        Mat3x2f(int id) {
            super(id);
        }

        public Mat3x2f set(Matrix3x2f value) {
            this.value = value;
            dirty = true;
            return this;
        }

        public Matrix3x2f get() {
            return value;
        }

        @Override
        protected void uniform() {
            FloatBuffer buffer = BufferUtils.createFloatBuffer(16);
            if(!LEGACY) GL21.glUniformMatrix3x2fv(id, false, value.get(buffer));
            else {
                try {
                    LEGACY_METHOD.invoke(null, id, false, value.get(buffer));
                } catch(IllegalAccessException | InvocationTargetException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    public static class Mat4f extends Uniform {
        private static Method LEGACY_METHOD;
        static {
            try {
                LEGACY_METHOD = GL20.class.getMethod("glUniformMatrix4", int.class, boolean.class, FloatBuffer.class);
            } catch(NoSuchMethodException e) {
                if(LEGACY) throw new RuntimeException(e);
            }
        }

        private Matrix4f value;

        Mat4f(int id) {
            super(id);
        }

        public Mat4f set(Matrix4f value) {
            this.value = value;
            dirty = true;
            return this;
        }

        public Matrix4f get() {
            return value;
        }

        @Override
        protected void uniform() {
            uniform(id, value);
        }

        static void uniform(int id, Matrix4f value) {
            FloatBuffer buffer = BufferUtils.createFloatBuffer(16);
            if(!LEGACY) GL20.glUniformMatrix4fv(id, false, buffer);
            else {
                try {
                    LEGACY_METHOD.invoke(null, id, false, value.get(buffer));
                } catch(IllegalAccessException | InvocationTargetException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    public static class Mat4x3f extends Uniform {
        private static Method LEGACY_METHOD;
        static {
            try {
                LEGACY_METHOD = GL21.class.getMethod("glUniformMatrix4x3", int.class, boolean.class, FloatBuffer.class);
            } catch(NoSuchMethodException e) {
                if(LEGACY) throw new RuntimeException(e);
            }
        }

        private Matrix4x3f value;

        Mat4x3f(int id) {
            super(id);
        }

        public Mat4x3f set(Matrix4x3f value) {
            this.value = value;
            dirty = true;
            return this;
        }

        public Matrix4x3f get() {
            return value;
        }

        @Override
        protected void uniform() {
            FloatBuffer buffer = BufferUtils.createFloatBuffer(12);
            if(!LEGACY) GL21.glUniformMatrix4x3fv(id, false, value.get(buffer));
            else {
                try {
                    LEGACY_METHOD.invoke(null, id, false, value.get(buffer));
                } catch(IllegalAccessException | InvocationTargetException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    protected final int id;
    protected boolean dirty = false;

    private Uniform(int id) {
        this.id = id;
    }

    protected abstract void uniform();

    void update() {
        if(dirty) {
            uniform();
            dirty = false;
        }
    }
}
