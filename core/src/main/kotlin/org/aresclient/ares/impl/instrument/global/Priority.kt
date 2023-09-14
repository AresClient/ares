package org.aresclient.ares.impl.instrument.global

import org.aresclient.ares.api.instrument.global.Global

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

    fun keyMatches(key: Prioritizer): Boolean = key == Priority.key

    fun hasPriority(key: Prioritizer): Boolean = Priority.key == null || Priority.key!!.priority() < key.priority()

    fun getCurrentPriority(): Int = if(key == null) -1 else key!!.priority()

    fun release(key: Prioritizer) {
        if(keyMatches(key)) released = true
    }

    fun isReleased(): Boolean = released
}