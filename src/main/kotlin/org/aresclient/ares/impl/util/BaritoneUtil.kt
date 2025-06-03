package org.aresclient.ares.impl.util

object BaritoneUtil {
    private val present by lazy {
        return@lazy try {
            Class.forName("baritone.api.BaritoneAPI")
            true
        } catch(_: ClassNotFoundException) {
            false
        }
    }

    fun isBaritonePresent(): Boolean = present
}
