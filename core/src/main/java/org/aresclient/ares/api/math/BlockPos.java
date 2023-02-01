package org.aresclient.ares.api.math;

import org.aresclient.ares.Ares;

/**
 * Provides a mesh interface for interfacing with a block position vec3i
 * @author Tigermouthbear 1/13/22
 */
public interface BlockPos extends Vec3i {
    static BlockPos create(int x, int y, int z) {
        return Ares.INSTANCE.creator.blockPos(x, y, z);
    }

    static BlockPos create(double x, double y, double z) {
        return create((int) Math.floor(x), (int) Math.floor(y), (int) Math.floor(z));
    }

    static BlockPos create(Vec3d vec) {
        return create(vec.getX(), vec.getY(), vec.getZ());
    }

    static BlockPos create(Vec3i source) {
        return create(source.getX(), source.getY(), source.getZ());
    }

    default BlockPos add(double x, double y, double z) {
        return x == 0.0D && y == 0.0D && z == 0.0D ? this : create((double) this.getX() + x, (double) this.getY() + y, (double) this.getZ() + z);
    }

    default BlockPos add(int x, int y, int z) {
        return x == 0 && y == 0 && z == 0 ? this : create(this.getX() + x, this.getY() + y, this.getZ() + z);
    }

    default BlockPos add(Vec3i vec) {
        return this.add(vec.getX(), vec.getY(), vec.getZ());
    }

    default BlockPos subtract(Vec3i vec) {
        return this.add(-vec.getX(), -vec.getY(), -vec.getZ());
    }

    default BlockPos up() {
        return this.up(1);
    }

    default BlockPos up(int n) {
        return this.offset(Facing.EnumValue.UP, n);
    }

    default BlockPos down() {
        return this.down(1);
    }

    default BlockPos down(int n) {
        return this.offset(Facing.EnumValue.DOWN, n);
    }

    default BlockPos north() {
        return this.north(1);
    }

    default BlockPos north(int n) {
        return this.offset(Facing.EnumValue.NORTH, n);
    }

    default BlockPos south() {
        return this.south(1);
    }

    default BlockPos south(int n) {
        return this.offset(Facing.EnumValue.SOUTH, n);
    }

    default BlockPos west() {
        return this.west(1);
    }

    default BlockPos west(int n) {
        return this.offset(Facing.EnumValue.WEST, n);
    }

    default BlockPos east() {
        return this.east(1);
    }

    default BlockPos east(int n) {
        return this.offset(Facing.EnumValue.EAST, n);
    }

    default BlockPos offset(Facing.EnumValue facing) {
        return this.offset(facing, 1);
    }

    default BlockPos offset(Facing.EnumValue facing, int n) {
        return n == 0 ? this : create(this.getX() + facing.asFacing().getOffset().getX() * n, this.getY() + facing.asFacing().getOffset().getY() * n, this.getZ() + facing.asFacing().getOffset().getZ() * n);
    }
}
