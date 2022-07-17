package org.aresclient.ares.utils

class Timer {
    var time: Long = -1L

    fun reset() {
        time = System.currentTimeMillis()
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

    fun setPassedMillis(time: Long) {
        this.time = System.currentTimeMillis() - time
    }

    fun setPassedTicks(time: Long) {
        this.time = System.currentTimeMillis() - (time * 50)
    }

    fun setPassedSeconds(time: Long) {
        this.time = System.currentTimeMillis() - (time * 1000)
    }

    fun getPassedMillis(): Long {
        return System.currentTimeMillis() - time
    }

    fun getPassedTicks(): Long {
        return (System.currentTimeMillis() - time) / 50
    }

    fun getPassedSeconds(): Long {
        return (System.currentTimeMillis() - time) / 1000
    }

    fun hasMillisPassed(time: Long): Boolean {
        return System.currentTimeMillis() - this.time >= time
    }

    fun hasTicksPassed(time: Long): Boolean {
        return System.currentTimeMillis() - this.time >= time * 50
    }

    fun hasSecondsPassed(time: Long): Boolean {
        return System.currentTimeMillis() - this.time >= time * 1000
    }
}
