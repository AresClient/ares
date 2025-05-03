package org.aresclient.ares.api.instruments

import org.aresclient.ares.api.Wrapper
import org.aresclient.ares.api.setting.MapSetting

abstract class Instrument(val name: String, val description: String, parentSettings: MapSetting): Wrapper {
	private val components = ArrayList<Component<*>>()
	val settings = parentSettings.addMap(name, description)

	open fun tick() {
	}

	internal fun addComponent(component: Component<*>) {
		components.add(component)
	}

	open fun registerEvents() {
		EVENTS.register(this)
		EVENTS.register(javaClass)

		components.forEach {
			EVENTS.register(it)
			EVENTS.register(it.javaClass)
		}
	}

	open fun unregisterEvents() {
		EVENTS.unregister(this)
		EVENTS.unregister(javaClass)

		components.forEach {
			EVENTS.unregister(it)
			EVENTS.unregister(it.javaClass)
		}
	}
}
