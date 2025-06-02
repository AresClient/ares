package org.aresclient.ares.api.instruments

import org.aresclient.ares.Ares

/**
 * Globals are used for grouping settings and functions which interact with multiple
 * modules but do not themselves directly do anything, and also holds related utility
 * functions to the purpose of the Global
 */
open class Global(val name: String, val description: String): Instrument(SETTINGS.addMap(name)) {

	companion object {
		private val SETTINGS = Ares.getSettings().addMap("Globals")
	}

	abstract class PriorityHandler<K: Prioritizer>(name: String, description: String): Global(name, description) {
		protected val keys: HashSet<K> = HashSet()

		fun begin(key: K): Boolean {
			if (keys.isNotEmpty()) {
				if (!keys.contains(key)) keys.add(key)
				return hasPriority(key)
			}

			keys.add(key)
			begin()

			return true
		}

		protected abstract fun begin()

		fun end(key: K) {
			if (keys.contains(key)) keys.remove(key)
			if (keys.isEmpty()) end()
		}

		protected abstract fun end()

		fun isActive(key: K): Boolean = keys.contains(key)

		fun hasPriority(key: K): Boolean = key == getCurrent()

		fun getCurrent(): K? {
			var key: K? = null
			keys.forEach {
				if (key == null) {
					key = it
					return@forEach
				}

				if (key!!.priority() < it.priority()) key = it
			}
			return key
		}
	}

	override fun isListening() = true
}

interface Prioritizer {
	fun priority(): Int
	fun interruptor(): Boolean = false
	fun onInterrupt() {}
}
