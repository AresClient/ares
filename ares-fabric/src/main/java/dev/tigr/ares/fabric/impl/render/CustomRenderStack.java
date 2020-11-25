package dev.tigr.ares.fabric.impl.render;

import dev.tigr.ares.core.util.render.IRenderStack;
import net.minecraft.client.util.math.MatrixStack;

/**
 * @author Tigermouthbear 11/20/20
 */
public class CustomRenderStack implements IRenderStack {
    private final MatrixStack matrixStack = new MatrixStack();

    @Override
    public void push() {
        matrixStack.push();
    }

    @Override
    public void pop() {
        matrixStack.pop();
    }

    @Override
    public void scale(double x, double y, double z) {
        matrixStack.scale((float) x, (float) y, (float) z);
    }

    @Override
    public void translate(double x, double y, double z) {
        matrixStack.translate(x, y, z);
    }

    @Override
    public void rotate(float angle, float x, float y, float z) {
        // idk
    }

    public MatrixStack getMatrixStack() {
        return matrixStack;
    }
}
