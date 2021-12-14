package dev.tigr.ares.core.util.math.floats;

import dev.tigr.ares.core.util.math.integers.V3I;
import dev.tigr.ares.core.util.math.longs.V3L;
import dev.tigr.ares.core.util.math.doubles.V3D;

public class V3F {
    public float x, y, z;

    public V3F(float x, float y, float z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public V3F(V3I v3I) {
        this.x = v3I.x;
        this.y = v3I.y;
        this.z = v3I.z;
    }

    public V3F(V3D v3D) {
        this.x = (float) v3D.x;
        this.y = (float) v3D.y;
        this.z = (float) v3D.z;
    }

    public V3F(V3L v3L) {
        this.x = v3L.x;
        this.y = v3L.y;
        this.z = v3L.z;
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public float getZ() {
        return z;
    }

    public void setX(float x) {
        this.x = x;
    }

    public void setY(float y) {
        this.y = y;
    }

    public void setZ(float z) {
        this.z = z;
    }
}
