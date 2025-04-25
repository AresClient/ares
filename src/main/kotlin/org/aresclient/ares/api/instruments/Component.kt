package org.aresclient.ares.api.instruments

import dev.tigr.simpleevents.event.Event
import dev.tigr.simpleevents.listener.EventHandler
import dev.tigr.simpleevents.listener.EventListener
import org.aresclient.ares.api.setting.SettingGroup

/**
 * Using components can help to break up a larger modules into smaller
 * module-specific utilities for ease of code readability and navigability.
 *
 * @param <I> The module a particular component pertains to
 */
open class Component<I: Instrument>(val master: I) {

	init {
		master.addComponent(this)
	}

	open class Function<I: Instrument>(master: I, private val function: () -> Unit): Component<I>(master) {
		fun run() = function.invoke()
	}

	/**
	 * @param <T> The type the function should return
	 */
	open class Returnable<I: Instrument, T>(master: I, private val returnableFunction: () -> T): Component<I>(master) {
		fun get(): T = returnableFunction.invoke()
	}

	/**
	 * @param <T> The type the function takes as a parameter
	 */
	open class Parameterized<I: Instrument, T>(master: I, private val parameterizedFunction: (T) -> Unit): Component<I>(master) {
		fun run(parameter: T) = parameterizedFunction.invoke(parameter)
	}

	/**
	 * @param <E> The event the listener should listen to
	 */
	open class Listener<I: Instrument, E: Event>(master: I, @EventHandler private val eventListener: EventListener<E>): Component<I>(master)

	open class Settings<I: Instrument>(master: I, val pathName: String): Component<I>(master) {
		val settings: SettingGroup = master.settings.addGroup(pathName)
	}
}