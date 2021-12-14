package dev.tigr.ares.core.util.math.floats;

import dev.tigr.ares.core.util.math.doubles.V2D;
import dev.tigr.ares.core.util.math.integers.V2I;
import dev.tigr.ares.core.util.math.longs.V2L;

public class V2F {
    public float a, b;

    public V2F(float a, float b) {
        this.a = a;
        this.b = b;
    }

    public V2F(V2I v2I) {
        this.a = v2I.a;
        this.b = v2I.b;
    }

    public V2F(V2D v2D) {
        this.a = (float) v2D.a;
        this.b = (float) v2D.b;
    }

    public V2F(V2L v2L) {
        this.a = v2L.a;
        this.b = v2L.b;
    }

    public float getA() {
        return a;
    }

    public float getB() {
        return b;
    }

    public void setA(float a) {
        this.a = a;
    }

    public void setP(float b) {
        this.b = b;
    }
}
