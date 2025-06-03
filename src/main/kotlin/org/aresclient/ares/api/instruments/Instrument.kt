package org.aresclient.ares.api.instruments

import net.minecraft.client.util.math.MatrixStack
import org.aresclient.ares.api.Wrapper
import org.aresclient.ares.api.nrender.drawer.HudDrawer
import org.aresclient.ares.api.nrender.drawer.WorldDrawer
import org.aresclient.ares.api.setting.MapSetting

abstract class Instrument(val settings: MapSetting): Wrapper {
	private val components = ArrayList<Component<*>>()

	internal fun addComponent(component: Component<*>) {
		components.add(component)
	}

	abstract fun isListening(): Boolean

	open fun registerEvents() {
		if(!isListening()) return // TODO: MIGHT NOT NEED THIS

		EVENTS.register(this)
		EVENTS.register(javaClass)

		components.forEach {
			EVENTS.register(it)
			EVENTS.register(it.javaClass)
		}
	}

	open fun unregisterEvents() {
		if(isListening()) return

		EVENTS.unregister(this)
		EVENTS.unregister(javaClass)

		components.forEach {
			EVENTS.unregister(it)
			EVENTS.unregister(it.javaClass)
		}
	}

	/* ---------------------------------------------------------------------- */

	fun tick() {
		if(isListening()) onTick()
		components.forEach(Component<*>::tick)
	}

	fun motion() {
		if(isListening()) onMotion()
		components.forEach(Component<*>::motion)
	}

	fun renderHud(drawer: HudDrawer, matrixStack: MatrixStack, delta: Float) {
		if(isListening()) onRenderHud(drawer, matrixStack, delta)
		components.forEach { it.renderHud(drawer, matrixStack, delta) }
	}

	fun renderWorld(drawer: WorldDrawer, delta: Float) {
		if(isListening()) onRenderWorld(drawer, delta)
		components.forEach { it.renderWorld(drawer, delta) }
	}

	/* ---------------------------------------------------------------------- */

	protected open fun onTick() {}
	protected open fun onMotion() {}
	protected open fun onRenderHud(drawer: HudDrawer, matrixStack: MatrixStack, delta: Float) {}
	protected open fun onRenderWorld(drawer: WorldDrawer, delta: Float) {}
}
