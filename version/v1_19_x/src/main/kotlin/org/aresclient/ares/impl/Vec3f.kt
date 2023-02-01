package org.aresclient.ares.impl

import org.aresclient.ares.api.math.Vec3f

class Vec3f(var xa: Float, var ya: Float, var za: Float): Vec3f {
    override fun getX(): Float = xa
    override fun getY(): Float = ya
    override fun getZ(): Float = za

    override fun setX(value: Float) {
        xa = value
    }

    override fun setY(value: Float) {
        ya = value
    }

    override fun setZ(value: Float) {
        za = value
    }
}