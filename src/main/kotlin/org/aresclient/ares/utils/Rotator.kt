package org.aresclient.ares.utils

interface Rotator {
    fun getRotationPriority(): Int
    fun getYawStep(): Float
    fun getPitchStep(): Float
}
