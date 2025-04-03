package org.aresclient.ares.impl

import org.aresclient.ares.api.Plugin
import org.aresclient.ares.impl.command.EchoCommand
import org.aresclient.ares.impl.command.HelpCommand
import org.aresclient.ares.impl.instrument.global.Interaction
import org.aresclient.ares.impl.instrument.global.Priority
import org.aresclient.ares.impl.instrument.global.Render
import org.aresclient.ares.impl.instrument.global.Rotation
import org.aresclient.ares.impl.instrument.module.modules.misc.ClickGUI
import org.aresclient.ares.impl.instrument.module.modules.misc.TitleScreen
import org.aresclient.ares.impl.instrument.module.modules.movement.Strafe
import org.aresclient.ares.impl.instrument.module.modules.offence.CrystalAura
import org.aresclient.ares.impl.instrument.module.modules.player.AntiAFK
import org.aresclient.ares.impl.instrument.module.modules.render.ESP
import org.aresclient.ares.impl.instrument.module.modules.render.TestModule

object Ares: Plugin() {

	override val name = "Ares"
	override val description = "The main Ares utility mod plugin"
	override val version = "3.0-1.21.5"
	override val authors = arrayOf("Tigermouthbear", "Makrennel")

	override fun init() {
		globals.addAll(arrayOf(
			Interaction,
			Priority,
			Render,
			Rotation
		))
		modules.addAll(arrayOf(
			ClickGUI,
			TitleScreen,

			Strafe,

			CrystalAura,

			AntiAFK,

			ESP,
			TestModule
		))
		commands.addAll(arrayOf(
			EchoCommand,
			HelpCommand
		))
	}

}