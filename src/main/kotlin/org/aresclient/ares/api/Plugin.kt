package org.aresclient.ares.api

import net.minecraft.client.util.math.MatrixStack
import org.aresclient.ares.api.instruments.Command
import org.aresclient.ares.api.instruments.Global
import org.aresclient.ares.api.instruments.Instrument
import org.aresclient.ares.api.instruments.Module

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

	open fun renderHud(matrixStack: MatrixStack, delta: Float) {
		modules.forEach { module -> module.renderHud(matrixStack, delta) }
	}

	open fun renderWorld(matrixStack: MatrixStack, delta: Float) {
		modules.forEach { module -> module.renderWorld(matrixStack, delta) }
	}
}
