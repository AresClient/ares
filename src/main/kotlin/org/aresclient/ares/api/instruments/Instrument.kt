package org.aresclient.ares.api.instruments

import org.aresclient.ares.api.Wrapper
import org.aresclient.ares.api.setting.SettingGroup

abstract class Instrument(name: String, description: String, parentSettings: SettingGroup): Wrapper {
	private val components = ArrayList<Component<*>>()
	val settings = parentSettings.addGroup(name, description)

	open fun tick() {
	}

	internal fun addComponent(component: Component<*>) {
		components.add(component)
	}

	open fun registerEvents() {
		EVENTS.register(this)
		EVENTS.register(javaClass)

		components.forEach {
			if(it is Component.Listener<*,*>) {
				EVENTS.register(it)
				EVENTS.register(it.javaClass)
			}
		}
	}

	open fun unregisterEvents() {
		EVENTS.unregister(this)
		EVENTS.unregister(javaClass)

		components.forEach {
			if(it is Component.Listener<*,*>) {
				EVENTS.unregister(it)
				EVENTS.unregister(it.javaClass)
			}
		}
	}
}
