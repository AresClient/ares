package org.aresclient.ares.utils

class MathHelper {
    companion object {
        fun lerp(delta: Float, start: Float, end: Float): Float {
            return start + (end - start) * delta
        }

        fun lerp(delta: Double, start: Double, end: Double): Double {
            return start + (end - start) * delta
        }
    }
}