package org.aresclient.ares.api.math;


import org.aresclient.ares.Ares;

/**
 * Provides a mesh interface for interfacing with a 3d box
 * @author Tigermouthbear 1/13/22
 */
public interface Box {
    static Box create(double x1, double y1, double z1, double x2, double y2, double z2) {
        return Ares.INSTANCE.creator.box(x1, y1, z1, x2, y2, z2);
    }

    static Box create(BlockPos pos) {
        return create(pos.getX(), pos.getY(), pos.getZ(), pos.getX() + 1, pos.getY() + 1, pos.getZ() + 1);
    }

    static Box create(BlockPos pos1, BlockPos pos2) {
        return create(pos1.getX(), pos1.getY(), pos1.getZ(), pos2.getX(), pos2.getY(), pos2.getZ());
    }

    static Box create(Vec3d pos1, Vec3d pos2) {
        return create(pos1.getX(), pos1.getY(), pos1.getZ(), pos2.getX(), pos2.getY(), pos2.getZ());
    }

    double getMinX();

    double getMinY();

    double getMinZ();

    double getMaxX();

    double getMaxY();

    double getMaxZ();

    default Box withMinX(double minX) {
        return create(minX, getMinY(), getMinZ(), getMaxX(), getMaxY(), getMaxZ());
    }

    default Box withMinY(double minY) {
        return create(getMinX(), minY, getMinZ(), getMaxX(), getMaxY(), getMaxZ());
    }

    default Box withMinZ(double minZ) {
        return create(getMinX(), getMinY(), minZ, getMaxX(), getMaxY(), getMaxZ());
    }

    default Box withMaxX(double maxX) {
        return create(getMinX(), getMinY(), getMinZ(), maxX, getMaxY(), getMaxZ());
    }

    default Box withMaxY(double maxY) {
        return create(getMinX(), getMinY(), getMinZ(), getMaxX(), maxY, getMaxZ());
    }

    default Box withMaxZ(double maxZ) {
        return create(getMinX(), getMinY(), getMinZ(), getMaxX(), getMaxY(), maxZ);
    }

    default Box shrink(double x, double y, double z) {
        double d0 = getMinX();
        double d1 = getMinY();
        double d2 = getMinZ();
        double d3 = getMaxX();
        double d4 = getMaxY();
        double d5 = getMaxZ();

        if(x < 0.0D) d0 -= x;
        else if (x > 0.0D) d3 -= x;
        if(y < 0.0D) d1 -= y;
        else if (y > 0.0D) d4 -= y;
        if(z < 0.0D) d2 -= z;
        else if (z > 0.0D) d5 -= z;

        return create(d0, d1, d2, d3, d4, d5);
    }

    default Box shrink(Vec3d vec3d) {
        return shrink(vec3d.getX(), vec3d.getY(), vec3d.getZ());
    }

    default Box stretch(double x, double y, double z) {
        double d0 = getMinX();
        double d1 = getMinY();
        double d2 = getMinZ();
        double d3 = getMaxX();
        double d4 = getMaxY();
        double d5 = getMaxZ();

        if(x < 0.0D) d0 += x;
        else if (x > 0.0D) d3 += x;
        if(y < 0.0D) d1 += y;
        else if (y > 0.0D) d4 += y;
        if(z < 0.0D) d2 += z;
        else if (z > 0.0D) d5 += z;

        return create(d0, d1, d2, d3, d4, d5);
    }

    default Box stretch(Vec3d vec3d) {
        return stretch(vec3d.getX(), vec3d.getY(), vec3d.getZ());
    }

    default Box grow(double x, double y, double z) {
        double d0 = getMinX() - x;
        double d1 = getMinY() - y;
        double d2 = getMinZ() - z;
        double d3 = getMaxX() + x;
        double d4 = getMaxY() + y;
        double d5 = getMaxZ() + z;
        return create(d0, d1, d2, d3, d4, d5);
    }

    default Box grow(double value) {
        return grow(value, value, value);
    }

    default Box grow(Vec3d vec3d) {
        return grow(vec3d.getX(), vec3d.getY(), vec3d.getZ());
    }

    default Box intersection(Box other) {
        double d0 = Math.max(getMinX(), other.getMinX());
        double d1 = Math.max(getMinY(), other.getMinY());
        double d2 = Math.max(getMinZ(), other.getMinZ());
        double d3 = Math.min(getMaxX(), other.getMaxX());
        double d4 = Math.min(getMaxY(), other.getMaxY());
        double d5 = Math.min(getMaxZ(), other.getMaxZ());
        return create(d0, d1, d2, d3, d4, d5);
    }

    default Box union(Box other) {
        double d0 = Math.min(getMinX(), other.getMinX());
        double d1 = Math.min(getMinY(), other.getMinY());
        double d2 = Math.min(getMinZ(), other.getMinZ());
        double d3 = Math.max(getMaxX(), other.getMaxX());
        double d4 = Math.max(getMaxY(), other.getMaxY());
        double d5 = Math.max(getMaxZ(), other.getMaxZ());
        return create(d0, d1, d2, d3, d4, d5);
    }

    default Box offset(double x, double y, double z) {
        return create(getMinX() + x, getMinY() + y, getMinZ() + z, getMaxX() + x, getMaxY() + y, getMaxZ() + z);
    }

    default Box offset(BlockPos blockPos) {
        return offset(blockPos.getX(), blockPos.getY(), blockPos.getZ());
    }

    default Box offset(Vec3d vec3d) {
        return offset(vec3d.getX(), vec3d.getY(), vec3d.getZ());
    }

    default boolean intersects(Box box) {
        return intersects(box.getMinX(), box.getMinY(), box.getMinZ(), box.getMaxX(), box.getMaxY(), box.getMaxZ());
    }

    default boolean intersects(double minX, double minY, double minZ, double maxX, double maxY, double maxZ) {
        return getMinX() < maxX && getMaxX() > minX && getMinY() < maxY && getMaxY() > minY && getMinZ() < maxZ && getMaxZ() > minZ;
    }

    default boolean intersects(Vec3d pos1, Vec3d pos2) {
        return intersects(Math.min(pos1.getX(), pos2.getX()), Math.min(pos1.getY(), pos2.getY()), Math.min(pos1.getZ(), pos2.getZ()),
                Math.max(pos1.getX(), pos2.getX()), Math.max(pos1.getY(), pos2.getY()), Math.max(pos1.getZ(), pos2.getZ()));
    }

    default boolean contains(double x, double y, double z) {
        return x >= getMinX() && x < getMaxX() && y >= getMinY() && y < getMaxY() && z >= getMinZ() && z < getMaxZ();
    }

    default boolean contains(Vec3d vec3d) {
        return contains(vec3d.getX(), vec3d.getY(), vec3d.getZ());
    }

    default double getAverageSideLength() {
        return (getXLength() + getYLength() + getZLength()) / 3D;
    }

    default double getXLength() {
        return getMaxX() - getMinX();
    }

    default double getYLength() {
        return getMaxY() - getMinY();
    }

    default double getZLength() {
        return getMaxZ() - getMinZ();
    }

    default Vec3d getCenter() {
        return Vec3d.create(getMinX() + (getMaxX() - getMinX()) * 0.5D, getMinY() + (getMaxY() - getMinY()) * 0.5D, getMinZ() + (getMaxZ() - getMinZ()) * 0.5D);
    }

    // TODO: RAYCASTING
}
