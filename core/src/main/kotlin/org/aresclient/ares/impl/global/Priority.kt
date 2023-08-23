package org.aresclient.ares.impl.global

import org.aresclient.ares.api.global.Global

interface Prioritizer {
    fun priority(): Int
    fun interruptor(): Boolean = false
    fun onInterrupt() {}
}

/**
 * TODO: Conditional priority? (so that things don't always get overtaken by higher priority while executing halfway)
 */
object Priority: Global("Priority", "Handles the priority of different modules so that Rotations and Interactions happen in an optimal order") {
    private var key: Prioritizer? = null
    private var released = true

    fun keyMatches(key: Prioritizer): Boolean = key == this.key

    fun hasPriority(key: Prioritizer): Boolean = this.key == null || this.key!!.priority() < key.priority()

    fun getCurrentPriority(): Int = if(key == null) -1 else key!!.priority()

    fun release(key: Prioritizer) {
        if(keyMatches(key)) released = true
    }

    fun isReleased(): Boolean = released
}