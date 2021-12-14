package dev.tigr.ares.core.util.math.doubles;

import dev.tigr.ares.core.util.math.integers.V2I;
import dev.tigr.ares.core.util.math.longs.V2L;
import dev.tigr.ares.core.util.math.floats.V2F;

public class V2D {
    public double a, b;

    public V2D(double a, double b) {
        this.a = a;
        this.b = b;
    }

    public V2D(V2I v2I) {
        this.a = v2I.a;
        this.b = v2I.b;
    }

    public V2D(V2F v2F) {
        this.a = v2F.a;
        this.b = v2F.b;
    }

    public V2D(V2L v2L) {
        this.a = v2L.a;
        this.b = v2L.b;
    }

    public double getA() {
        return a;
    }

    public double getB() {
        return b;
    }

    public V2D setA(double a) {
        this.a = a;
        return this;
    }

    public V2D setB(double b) {
        this.b = b;
        return this;
    }
}
