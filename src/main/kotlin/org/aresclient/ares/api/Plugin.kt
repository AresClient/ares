package org.aresclient.ares.api

import org.aresclient.ares.Main
import org.aresclient.ares.api.command.Command
import org.aresclient.ares.api.instruments.Global
import org.aresclient.ares.api.instruments.Instrument
import org.aresclient.ares.api.instruments.Module
import org.aresclient.ares.api.setting.Setting
import org.slf4j.Logger
import org.slf4j.LoggerFactory

abstract class Plugin: Wrapper {

	private val start:Long = System.currentTimeMillis()

	abstract val name:String
	abstract val description:String
	abstract val version:String
	abstract val authors:Array<String>

	val settings:Setting.Map<*> = Main.PLUGIN_SETTINGS.addMap(name)

	val logger:Logger = LoggerFactory.getLogger(name)

	val globals:MutableList<Global> = ArrayList()
	val modules:MutableList<Module> = ArrayList()
	val commands:MutableList<Command> = ArrayList()

	abstract fun init()

	init {
		init()

		globals.forEach(Instrument::registerEvents)
		modules.forEach(Instrument::registerEvents)

		Main.PLUGINS.add(this)

		logger.info(
			"Ares-Main loaded plugin {} with {} globals, {} modules and {} commands in {} millisecond.",
			name, globals.size, modules.size, commands.size,
			System.currentTimeMillis() - start
		)
	}

}