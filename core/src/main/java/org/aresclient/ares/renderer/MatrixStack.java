package org.aresclient.ares.renderer;

import org.joml.Matrix4f;

import java.util.Stack;

public class MatrixStack extends Stack<MatrixStack.Element> {
    public static final MatrixStack EMPTY = new MatrixStack();

    public static class Element {
        private final Matrix4f projection = new Matrix4f();
        private boolean projectionDirty = true;
        private final Matrix4f model = new Matrix4f();
        private boolean modelDirty = true;

        Element(Matrix4f projection, Matrix4f model) {
            if(projection != null) this.projection.set(projection);
            if(model != null) this.model.set(model);
        }

        public Matrix4f projection() {
            return projection;
        }

        public Matrix4f model() {
            return model;
        }

        public void projection(Matrix4f projection) {
            this.projection.set(projection);
            projectionDirty = true;
        }

        public void model(Matrix4f model) {
            this.model.set(model);
            modelDirty = true;
        }
    }

    public MatrixStack() {
        reset();
    }

    public void reset() {
        push(new Element(null, null));
    }

    public void resetModel() {
        push(new Element(peek().projection, null));
    }

    public void resetProjection() {
        push(new Element(null, peek().model));
    }

    public void push() {
        Element element = peek();
        push(new Element(element.projection, element.model));
    }

    public Matrix4f projection() {
        return peek().projection;
    }

    public Matrix4f model() {
        return peek().model;
    }

    public MatrixStack projection(Matrix4f projection) {
        peek().projection(projection);
        return this;
    }

    public MatrixStack model(Matrix4f model) {
        peek().model(model);
        return this;
    }

    void update(int projection, int model) {
        Element element = peek();
        if(element.projectionDirty) Uniform.Mat4f.uniform(projection, element.projection);
        if(element.modelDirty) Uniform.Mat4f.uniform(model, element.model);
    }
}
