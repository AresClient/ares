package org.aresclient.ares.api.math;

import org.aresclient.ares.Ares;

public interface Vec3f {
    Vec3f ZERO = create(0, 0, 0);

    static Vec3f create(float x, float y, float z) {
        return Ares.INSTANCE.creator.vec3f(x, y, z);
    }

    static Vec3f create(double x, double y, double z) {
        return Ares.INSTANCE.creator.vec3f((float) x, (float) y, (float) z);
    }

    static Vec3f create(Vec3d vec3d) {
        return Ares.INSTANCE.creator.vec3f((float) vec3d.getX(), (float) vec3d.getY(), (float) vec3d.getZ());
    }

    static Vec3f create(Vec3i vec3i) {
        return Ares.INSTANCE.creator.vec3f(vec3i.getX(), vec3i.getY(), vec3i.getZ());
    }

    float getX();
    float getY();
    float getZ();

    void setX(float value);
    void setY(float value);
    void setZ(float value);

    // helper functions below

    default Vec3f subtractReverse(Vec3f vec) {
        return create(vec.getX() - getX(), vec.getY() - getY(), vec.getZ() - getZ());
    }

    default Vec3f normalize() {
        float d0 = (float) Math.sqrt(getX() * getX() + getY() * getY() + getZ() * getZ());
        return d0 < 1.0E-4D ? ZERO : create(getX() / d0, getY() / d0, getZ() / d0);
    }

    default float dotProduct(Vec3f vec) {
        return getX() * vec.getX() + getY() * vec.getY() + getZ() * vec.getZ();
    }

    default Vec3f crossProduct(Vec3f vec) {
        return create(getY() * vec.getZ() - getZ() * vec.getY(), getZ() * vec.getX() - getX() * vec.getZ(), getX() * vec.getY() - getY() * vec.getX());
    }

    default Vec3f subtract(Vec3f vec) {
        return subtract(vec.getX(), vec.getY(), vec.getZ());
    }

    default Vec3f subtract(float x, float y, float z) {
        return add(-x, -y, -z);
    }

    default Vec3f add(Vec3f vec) {
        return add(vec.getX(), vec.getY(), vec.getZ());
    }

    default Vec3f add(float x, float y, float z) {
        return create(getX() + x, getY() + y, getZ() + z);
    }

    default double distanceTo(Vec3f vec) {
        float d0 = vec.getX() - getX();
        float d1 = vec.getY() - getY();
        float d2 = vec.getZ() - getZ();
        return Math.sqrt(d0 * d0 + d1 * d1 + d2 * d2);
    }

    default double distanceTo(float x, float y, float z) {
        double d0 = x - getX();
        double d1 = y - getY();
        double d2 = z - getZ();
        return Math.sqrt(d0 * d0 + d1 * d1 + d2 * d2);
    }

    default float squareDistanceTo(Vec3f vec) {
        float d0 = vec.getX() - getX();
        float d1 = vec.getY() - getY();
        float d2 = vec.getZ() - getZ();
        return d0 * d0 + d1 * d1 + d2 * d2;
    }

    default float squareDistanceTo(float xIn, float yIn, float zIn) {
        float d0 = xIn - getX();
        float d1 = yIn - getY();
        float d2 = zIn - getZ();
        return d0 * d0 + d1 * d1 + d2 * d2;
    }

    default Vec3f scale(float factor) {
        return create(getX() * factor, getY() * factor, getZ() * factor);
    }

    default double length() {
        return Math.sqrt(lengthSquared());
    }

    default float lengthSquared() {
        return getX() * getX() + getY() * getY() + getZ() * getZ();
    }

    default int getHashCode() {
        int i = Float.floatToIntBits(getX());
        i = 31 * i + Float.floatToIntBits(getY());
        i = 31 * i + Float.floatToIntBits(getZ());
        return i;
    }

    default String getString() {
        return "(" + getX() + ", " + getY() + ", " + getZ() + ")";
    }
}

