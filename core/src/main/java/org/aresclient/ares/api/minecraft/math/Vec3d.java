package org.aresclient.ares.api.minecraft.math;

import org.aresclient.ares.AresStatics;

public interface Vec3d {
    Vec3d ZERO = create(0,0,0);

    static Vec3d create(double x, double y, double z) {
        return AresStatics.createVec3d(x, y, z);
    }

    double getX();
    double getY();
    double getZ();

    /** Do not rely on these provided setters when setting vectors passed by mesh to mods */
    void setX(double value);
    /** Do not rely on these provided setters when setting vectors passed by mesh to mods */
    void setY(double value);
    /** Do not rely on these provided setters when setting vectors passed by mesh to mods */
    void setZ(double value);

    /** Do not rely on these provided setters when setting vectors passed by mesh to mods */
    default void set(Vec3d vec3d) {
        set(vec3d.getX(), vec3d.getY(), vec3d.getZ());
    }

    /** Do not rely on these provided setters when setting vectors passed by mesh to mods */
    default void set(double x, double y, double z) {
        setX(x);
        setY(y);
        setZ(z);
    }

    default Vec3d subtractReverse(Vec3d vec) {
        return create(vec.getX() - getX(), vec.getY() - getY(), vec.getZ() - getZ());
    }

    default Vec3d normalize() {
        double d0 = Math.sqrt(getX() * getX() + getY() * getY() + getZ() * getZ());
        return d0 < 1.0E-4D ? ZERO : create(getX() / d0, getY() / d0, getZ() / d0);
    }

    default double dotProduct(Vec3d vec) {
        return getX() * vec.getX() + getY() * vec.getY() + getZ() * vec.getZ();
    }

    default Vec3d crossProduct(Vec3d vec) {
        return create(getY() * vec.getZ() - getZ() * vec.getY(), getZ() * vec.getX() - getX() * vec.getZ(), getX() * vec.getY() - getY() * vec.getX());
    }

    default Vec3d subtract(Vec3i vec) {
        return subtract(vec.getX(), vec.getY(), vec.getZ());
    }

    default Vec3d subtract(Vec3d vec) {
        return subtract(vec.getX(), vec.getY(), vec.getZ());
    }

    default Vec3d subtract(double x, double y, double z) {
        return add(-x, -y, -z);
    }

    default Vec3d add(Vec3i vec) {
        return add(vec.getX(), vec.getY(), vec.getZ());
    }

    default Vec3d add(Vec3d vec) {
        return add(vec.getX(), vec.getY(), vec.getZ());
    }

    default Vec3d add(double x, double y, double z) {
        return create(getX() + x, getY() + y, getZ() + z);
    }

    default double distanceTo(Vec3d vec) {
        double d0 = vec.getX() - getX();
        double d1 = vec.getY() - getY();
        double d2 = vec.getZ() - getZ();
        return Math.sqrt(d0 * d0 + d1 * d1 + d2 * d2);
    }

    default double distanceTo(double x, double y, double z) {
        double d0 = x - getX();
        double d1 = y - getY();
        double d2 = z - getZ();
        return Math.sqrt(d0 * d0 + d1 * d1 + d2 * d2);
    }

    default double squareDistanceTo(Vec3d vec) {
        double d0 = vec.getX() - getX();
        double d1 = vec.getY() - getY();
        double d2 = vec.getZ() - getZ();
        return d0 * d0 + d1 * d1 + d2 * d2;
    }

    default double squareDistanceTo(double xIn, double yIn, double zIn) {
        double d0 = xIn - getX();
        double d1 = yIn - getY();
        double d2 = zIn - getZ();
        return d0 * d0 + d1 * d1 + d2 * d2;
    }

    default Vec3d scale(double factor) {
        return create(getX() * factor, getY() * factor, getZ() * factor);
    }

    default double length() {
        return Math.sqrt(lengthSquared());
    }

    default double lengthSquared() {
        return getX() * getX() + getY() * getY() + getZ() * getZ();
    }

    default int getHashCode() {
        long j = Double.doubleToLongBits(getX());
        int i = (int) (j ^ j >>> 32);
        j = Double.doubleToLongBits(getY());
        i = 31 * i + (int) (j ^ j >>> 32);
        j = Double.doubleToLongBits(getZ());
        i = 31 * i + (int) (j ^ j >>> 32);
        return i;
    }

    default String getString() {
        return "(" + getX() + ", " + getY() + ", " + getZ() + ")";
    }
}
