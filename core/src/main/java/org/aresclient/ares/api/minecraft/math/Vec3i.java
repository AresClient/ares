package org.aresclient.ares.api.minecraft.math;

import org.aresclient.ares.AresStatics;

public interface Vec3i {
    static Vec3i create(int x, int y, int z) {
        return AresStatics.createVec3i(x, y, z);
    }

    static Vec3i create(double x, double y, double z) {
        return AresStatics.createVec3i((int) Math.floor(x), (int) Math.floor(y), (int) Math.floor(z));
    }

    static Vec3i create(Vec3d vec3d) {
        return create(vec3d.getX(), vec3d.getY(), vec3d.getZ());
    }

    default int getHashCode() {
        return (getY() + getZ() * 31) * 31 + getX();
    }

    default int compareTo(Vec3i other) {
        if(getY() == other.getY()) return getZ() == other.getZ() ? getX() - other.getX() : getZ() - other.getZ();
        else return getY() - other.getY();
    }

    int getX();
    int getY();
    int getZ();

    void setX(int value);
    void setY(int value);
    void setZ(int value);

    default void set(Vec3i vec3i) {
        set(vec3i.getX(), vec3i.getY(), vec3i.getZ());
    }

    default void set(int x, int y, int z) {
        setX(x);
        setY(y);
        setZ(z);
    }

    default Vec3i crossProduct(Vec3i vec) {
        return create(getY() * vec.getZ() - getZ() * vec.getY(), getZ() * vec.getX() - getX() * vec.getZ(), getX() * vec.getY() - getY() * vec.getX());
    }

    default double distanceTo(Vec3f vec) {
        float d0 = vec.getX() - getX();
        float d1 = vec.getY() - getY();
        float d2 = vec.getZ() - getZ();
        return Math.sqrt(d0 * d0 + d1 * d1 + d2 * d2);
    }

    default double distanceTo(int x, int y, int z) {
        int d0 = x - getX();
        int d1 = y - getY();
        int d2 = z - getZ();
        return Math.sqrt(d0 * d0 + d1 * d1 + d2 * d2);
    }

    default int squareDistanceTo(Vec3i vec) {
        int d0 = vec.getX() - getX();
        int d1 = vec.getY() - getY();
        int d2 = vec.getZ() - getZ();
        return d0 * d0 + d1 * d1 + d2 * d2;
    }

    default float squareDistanceTo(int xIn, int yIn, int zIn) {
        int d0 = xIn - getX();
        int d1 = yIn - getY();
        int d2 = zIn - getZ();
        return d0 * d0 + d1 * d1 + d2 * d2;
    }

    default String getString() {
        return "(" + getX() + ", " + getY() + ", " + getZ() + ")";
    }
}
