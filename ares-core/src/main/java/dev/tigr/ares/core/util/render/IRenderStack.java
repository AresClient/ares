package dev.tigr.ares.core.util.render;

/**
 * @author Tigermouthbear
 * provides abstraction for opengl matrix stack
 */
public interface IRenderStack {
    void push();

    void pop();

    void scale(double x, double y, double z);

    void translate(double x, double y, double z);

    void rotate(float angle, float x, float y, float z);
}
