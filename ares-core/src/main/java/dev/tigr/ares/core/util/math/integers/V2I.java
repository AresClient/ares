package dev.tigr.ares.core.util.math.integers;

import dev.tigr.ares.core.util.math.longs.V2L;
import dev.tigr.ares.core.util.math.doubles.V2D;
import dev.tigr.ares.core.util.math.floats.V2F;

public class V2I {
    public int a, b;

    public V2I(int a, int b) {
        this.a = a;
        this.b = b;
    }

    public V2I(V2F v2F) {
        this.a = (int) v2F.a;
        this.b = (int) v2F.b;
    }

    public V2I(V2D v2D) {
        this.a = (int) v2D.a;
        this.b = (int) v2D.b;
    }

    public V2I(V2L v2L) {
        this.a = (int) v2L.a;
        this.b = (int) v2L.b;
    }

    public int getA() {
        return a;
    }

    public int getB() {
        return b;
    }

    public void setA(int a) {
        this.a = a;
    }

    public void setB(int b) {
        this.b = b;
    }

    public V2D getMiddle() {
        return new V2D(this.a + 0.5, this.b + 0.5);
    }
}
