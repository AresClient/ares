package dev.tigr.ares.core.util.math.longs;

import dev.tigr.ares.core.util.math.doubles.V2D;
import dev.tigr.ares.core.util.math.floats.V2F;
import dev.tigr.ares.core.util.math.integers.V2I;

public class V2L {
    public long a, b;

    public V2L(long a, long b) {
        this.a = a;
        this.b = b;
    }

    public V2L(V2I v2I) {
        this.a = v2I.a;
        this.b = v2I.b;
    }

    public V2L(V2F v2F) {
        this.a = (long) v2F.a;
        this.b = (long) v2F.b;
    }

    public V2L(V2D v2D) {
        this.a = (long) v2D.a;
        this.b = (long) v2D.b;
    }

    public long getA() {
        return a;
    }

    public long getB() {
        return b;
    }

    public void setA(long a) {
        this.a = a;
    }

    public void setB(long b) {
        this.b = b;
    }
}
