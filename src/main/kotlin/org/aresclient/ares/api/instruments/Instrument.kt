package org.aresclient.ares.api.instruments

import org.aresclient.ares.api.Wrapper
import org.aresclient.ares.api.setting.Setting

abstract class Instrument(val name: String, val description: String, parentSettings: Setting.Map<*>): Wrapper {
	val components = ArrayList<Component<*>>()
	val settings:Setting.Map<*>

	init {
		settings = parentSettings.addMap(name)
		settings.setDescription(description)
	}

	open fun tick() {
	}

	internal fun addComponent(component:Component<*>) {
		components.add(component)
		if (component is Component.Settings<*>) {
			component.settings = settings.addMap(component.pathName)
		}
	}

	open fun registerEvents() {
		EVENTS.register(this)
		EVENTS.register(javaClass)

		components.forEach {
			if (it is Component.Listener<*,*>) {
				EVENTS.register(it)
				EVENTS.register(it.javaClass)
			}
		}
	}

	open fun unregisterEvents() {
		EVENTS.unregister(this)
		EVENTS.unregister(javaClass)

		components.forEach {
			if (it is Component.Listener<*,*>) {
				EVENTS.unregister(it)
				EVENTS.unregister(it.javaClass)
			}
		}
	}

}