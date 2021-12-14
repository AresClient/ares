package dev.tigr.ares.core.util.math.doubles;

import dev.tigr.ares.core.util.math.floats.V3F;
import dev.tigr.ares.core.util.math.integers.V3I;
import dev.tigr.ares.core.util.math.longs.V3L;

public class V3D {
    public double x, y, z;

    public V3D(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public V3D(V3I v3I) {
        this.x = v3I.x;
        this.y = v3I.y;
        this.z = v3I.z;
    }

    public V3D(V3F v3F) {
        this.x = v3F.x;
        this.y = v3F.y;
        this.z = v3F.z;
    }

    public V3D(V3L v3L) {
        this.x = v3L.x;
        this.y = v3L.y;
        this.z = v3L.z;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getZ() {
        return z;
    }

    public V3D setX(double x) {
        this.x = x;
        return this;
    }

    public V3D setY(double y) {
        this.y = y;
        return this;
    }

    public V3D setZ(double z) {
        this.z = z;
        return this;
    }
}
