package org.aresclient.ares.api.instruments

import org.aresclient.ares.Ares

/**
 * Globals are used for grouping settings and functions which interact with multiple
 * modules but do not themselves directly do anything, and also holds related utility
 * functions to the purpose of the Global
 */
abstract class Global(name:String, description:String
): Instrument(name, description, SETTINGS) {

	companion object {
		private val SETTINGS = Ares.SETTINGS.addMap("Globals")
	}

}

