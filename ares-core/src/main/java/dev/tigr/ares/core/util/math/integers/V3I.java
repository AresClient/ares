package dev.tigr.ares.core.util.math.integers;

import dev.tigr.ares.core.util.math.longs.V3L;
import dev.tigr.ares.core.util.math.doubles.V3D;
import dev.tigr.ares.core.util.math.floats.V3F;

public class V3I {
    public int x, y, z;

    public V3I(int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public V3I(float x, float y, float z) {
        this.x = (int) x;
        this.y = (int) y;
        this.z = (int) z;
    }

    public V3I(double x, double y, double z) {
        this.x = (int) x;
        this.y = (int) y;
        this.z = (int) z;
    }

    public V3I(long x, long y, long z) {
        this.x = (int) x;
        this.y = (int) y;
        this.z = (int) z;
    }

    public V3I(V3F v3F) {
        this.x = (int) v3F.x;
        this.y = (int) v3F.y;
        this.z = (int) v3F.z;
    }

    public V3I(V3D v3D) {
        this.x = (int) v3D.x;
        this.y = (int) v3D.y;
        this.z = (int) v3D.z;
    }

    public V3I(V3L v3L) {
        this.x = (int) v3L.x;
        this.y = (int) v3L.y;
        this.z = (int) v3L.z;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getZ() {
        return z;
    }

    public void setX(int x) {
        this.x = x;
    }

    public void setY(int y) {
        this.y = y;
    }

    public void setZ(int z) {
        this.z = z;
    }

    public V3I offset(String axis, int distance) {
        if(distance == 0) return this;
        else {
            int
                    x1 = axis.equalsIgnoreCase("x") ? distance : 0,
                    y1 = axis.equalsIgnoreCase("y") ? distance : 0,
                    z1 = axis.equalsIgnoreCase("z") ? distance : 0;
            return new V3I(x + x1, y + y1, z + z1);
        }
    }

    public V3D getCenter() {
        return new V3D(this.x + 0.5, this.y + 0.5, this.z + 0.5);
    }

    public V3D getBottomCenter() {
        return new V3D(this.x + 0.5, this.y, this.z + 0.5);
    }

    public V3D getTopCenter() {
        return new V3D(this.x + 0.5, this.y + 1, this.z + 0.5);
    }
}
