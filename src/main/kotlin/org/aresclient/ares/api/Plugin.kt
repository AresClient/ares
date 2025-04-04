package org.aresclient.ares.api

import org.aresclient.ares.api.command.Command
import org.aresclient.ares.api.instruments.Global
import org.aresclient.ares.api.instruments.Instrument
import org.aresclient.ares.api.instruments.Module
import org.aresclient.ares.api.render.MatrixStack
import org.aresclient.ares.api.render.Renderer

open class Plugin(val name: String, val description: String, val version: String, val mcVersion: String,
				  val authors: Array<String>, val globals: List<Global> = emptyList(), val modules: List<Module> = emptyList(),
				  val commands: List<Command> = emptyList()): Wrapper {
	open fun init() {
	}

	open fun tickClient() {
		modules.forEach(Instrument::tick)
		globals.forEach(Instrument::tick)
	}

	open fun tickMotion() {
		modules.forEach(Module::motion)
	}

	open fun renderHud(delta: Float, buffers: Renderer.Buffers, matrixStack: MatrixStack) {
		modules.forEach { module -> module.renderHud(delta, buffers, matrixStack) }
	}

	open fun renderWorld(delta: Float, buffers: Renderer.Buffers, matrixStack: MatrixStack) {
		modules.forEach { module -> module.renderWorld(delta, buffers, matrixStack) }
	}
}
