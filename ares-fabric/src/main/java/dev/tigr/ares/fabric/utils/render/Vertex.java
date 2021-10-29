package dev.tigr.ares.fabric.utils.render;

import dev.tigr.ares.core.util.render.Color;
import net.minecraft.util.math.Vec3d;

public class Vertex {
    public double x, y, z;
    float r, g, b, a;

    public Vertex(double x, double y, double z, float r, float g, float b, float a) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.r = r;
        this.g = g;
        this.b = b;
        this.a = a;
    }

    public Vertex(double x, double y, double z, Color color) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.r = color.getRed();
        this.g = color.getGreen();
        this.b = color.getBlue();
        this.a = color.getAlpha();
    }

    public Vertex(Vec3d pos, float r, float g, float b, float a) {
        this.x = pos.getX();
        this.y = pos.getY();
        this.z = pos.getZ();
        this.r = r;
        this.g = g;
        this.b = b;
        this.a = a;
    }

    public Vertex(Vec3d pos, Color color) {
        this.x = pos.getX();
        this.y = pos.getY();
        this.z = pos.getZ();
        this.r = color.getRed();
        this.g = color.getGreen();
        this.b = color.getBlue();
        this.a = color.getAlpha();
    }

    public Vec3d getPos() {
        return new Vec3d(x, y, z);
    }

    public Vertex offset(double x, double y, double z) {
        return new Vertex(this.x + x, this.y + y, this.z + z, r, g, b, a);
    }

    public Color getColor() {
        return new Color(r, g, b, a);
    }

    public Vertex withColor(Color color) {
        return new Vertex(x, y, z, color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha());
    }

    public Vertex withColor(float r, float g, float b, float a) {
        return new Vertex(x, y, z, r, g, b, a);
    }

    public Vertex withAlpha(float alpha) {
        return new Vertex(x, y, z, r, g, b, alpha);
    }

    public Vertex asTransparent() {
        return new Vertex(x, y, z, r, g, b, 0);
    }

    public void setColor(Color color) {
        this.r = color.getRed();
        this.g = color.getGreen();
        this.b = color.getBlue();
        this.a = color.getAlpha();
    }

    public void setColor(float r, float g, float b, float a) {
        this.r = r;
        this.g = g;
        this.b = b;
        this.a = a;
    }

    public void setAlpha(float alpha) {
        this.a = alpha;
    }
}
