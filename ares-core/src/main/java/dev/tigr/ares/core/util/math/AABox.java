package dev.tigr.ares.core.util.math;

public class AABox {
    public double minX, minY, minZ, maxX, maxY, maxZ;

    public AABox(double minX, double minY, double minZ, double maxX, double maxY, double maxZ) {
        this.minX = minX;
        this.minY = minY;
        this.minZ = minZ;
        this.maxX = maxX;
        this.maxY = maxY;
        this.maxZ = maxZ;
    }
}
