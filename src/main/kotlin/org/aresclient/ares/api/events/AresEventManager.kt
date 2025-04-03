package org.aresclient.ares.api.events

import dev.tigr.simpleevents.EventManager
import dev.tigr.simpleevents.event.Event
import org.aresclient.ares.api.Wrapper

enum class Era { BEFORE, AFTER }
abstract class AresEvent(val name: String, val era: Era? = null): Event(), Wrapper
class AresEventManager: EventManager(), Wrapper {

	override fun <T: Any?> post(event: T): T = when (event) {
		is AresEvent -> {
			profiler.push("ares_" + event.name.lowercase() + event.era?.let { "_" + it.name.lowercase() })
			val out = super.post(event)
			profiler.pop()
			out
		}
		else         -> {
			super.post(event)
		}
	}

}

