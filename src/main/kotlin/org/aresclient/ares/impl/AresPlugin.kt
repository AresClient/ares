package org.aresclient.ares.impl

import org.aresclient.ares.api.Plugin
import org.aresclient.ares.impl.command.EchoCommand
import org.aresclient.ares.impl.command.HelpCommand
import org.aresclient.ares.impl.instrument.global.*
import org.aresclient.ares.impl.instrument.module.modules.misc.ClickGUI
import org.aresclient.ares.impl.instrument.module.modules.misc.TitleScreen
import org.aresclient.ares.impl.instrument.module.modules.movement.Speed
import org.aresclient.ares.impl.instrument.module.modules.offence.CrystalAura
import org.aresclient.ares.impl.instrument.module.modules.player.AntiAFK
import org.aresclient.ares.impl.instrument.module.modules.player.Freecam
import org.aresclient.ares.impl.instrument.module.modules.render.ESP
import org.aresclient.ares.impl.instrument.module.modules.render.Fullbright
import org.aresclient.ares.impl.instrument.module.modules.render.TestModule
import org.aresclient.ares.impl.instrument.module.modules.render.Tracers

object AresPlugin: Plugin(
	"Ares",
	"The main Ares utility mod plugin",
	"3.0.0-SNAPSHOT",
	"1.21.5",
	arrayOf("Tigermouthbear", "Makrennel"),
	globals = listOf(
		Camera,
		Interaction,
		Rotation
	),
	modules = listOf(
		ClickGUI,
		TitleScreen,

		Speed,

		CrystalAura,

		AntiAFK,
		Freecam,

		ESP,
		TestModule,
		Tracers,
		Fullbright
	),
	commands = listOf(
		EchoCommand,
		HelpCommand
	)
)