package org.aresclient.ares.api

import org.aresclient.ares.api.instruments.Command
import org.aresclient.ares.api.instruments.Global
import org.aresclient.ares.api.instruments.Instrument
import org.aresclient.ares.api.instruments.Module
import org.aresclient.ares.api.render.Renderer.State
import org.joml.Matrix4f

open class Plugin(val name: String, val description: String, val version: String, val mcVersion: String,
				  val authors: Array<String>, val globals: List<Global> = emptyList(), val modules: List<Module> = emptyList(),
				  val commands: List<Command> = emptyList()): Wrapper {
	open fun init() {
		modules.forEach {
			it.category.modules.add(it)
		}
	}

	open fun tickClient() {
		modules.forEach(Instrument::tick)
		globals.forEach(Instrument::tick)
	}

	open fun tickMotion() {
		modules.forEach(Module::motion)
	}

	open fun renderHud(delta: Float, renderer: State) {
		modules.forEach { module -> module.renderHud(delta, renderer) }
	}

	open fun renderWorld3d(delta: Float, renderer: State) {
		modules.forEach { module -> module.renderWorld3d(delta, renderer) }
	}

	open fun renderWorld2d(delta: Float, renderer: State, projection: Matrix4f) {
		modules.forEach { module -> module.renderWorld2d(delta, renderer, projection) }
	}
}
