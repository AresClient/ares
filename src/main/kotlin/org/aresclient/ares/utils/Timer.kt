package org.aresclient.ares.utils

class Timer {
    var time: Long = -1L

    fun reset() {
        time = System.currentTimeMillis()
    }

    fun setPassedMillis(time: Long) {
        this.time = System.currentTimeMillis() - time
    }

    fun setPassedTicks(time: Long) {
        this.time = System.currentTimeMillis() - (time * 50)
    }

    fun setPassedSec(time: Long) {
        this.time = System.currentTimeMillis() - (time * 1000)
    }

    fun getMillis(): Long {
        return time
    }

    fun getTicks(): Long {
        return time / 50
    }

    fun getSec(): Long {
        return time / 1000
    }

    fun passedMillis(time: Long): Boolean {
        return System.currentTimeMillis() - this.time >= time
    }

    fun passedTicks(time: Long): Boolean {
        return System.currentTimeMillis() - this.time >= time * 50
    }

    fun passedSec(time: Long): Boolean {
        return System.currentTimeMillis() - this.time >= time * 1000
    }
}