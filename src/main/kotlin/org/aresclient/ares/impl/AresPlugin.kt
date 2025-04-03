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

object AresPlugin: Plugin(
	"Ares",
	"The main Ares utility mod plugin",
	"3.0.0-SNAPSHOT",
	"1.21.5",
	arrayOf("Tigermouthbear", "Makrennel"),
	globals = listOf(
		Interaction,
		Priority,
		Render,
		Rotation
	),
	modules = listOf(
		ClickGUI,
		TitleScreen,

		Strafe,

		CrystalAura,

		AntiAFK,

		ESP,
		TestModule
	),
	commands = listOf(
		EchoCommand,
		HelpCommand
	)
)