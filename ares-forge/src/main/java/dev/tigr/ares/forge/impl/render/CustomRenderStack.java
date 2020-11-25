package dev.tigr.ares.forge.impl.render;

import dev.tigr.ares.core.util.render.IRenderStack;
import net.minecraft.client.renderer.GlStateManager;

/**
 * @author Tigermouthbear 11/19/20
 */
public class CustomRenderStack implements IRenderStack {
    @Override
    public void push() {
        GlStateManager.pushMatrix();
    }

    @Override
    public void pop() {
        GlStateManager.popMatrix();
    }

    @Override
    public void scale(double x, double y, double z) {
        GlStateManager.scale(x, y, z);
    }

    @Override
    public void translate(double x, double y, double z) {
        GlStateManager.translate(x, y, z);
    }

    @Override
    public void rotate(float angle, float x, float y, float z) {
        GlStateManager.rotate(angle, x, y, z);
    }
}
