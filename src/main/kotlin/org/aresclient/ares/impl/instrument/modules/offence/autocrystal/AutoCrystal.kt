package org.aresclient.ares.impl.instrument.modules.offence.autocrystal

import org.aresclient.ares.api.instruments.Module

object AutoCrystal: Module(Category.OFFENSE, "Auto Crystal", "Automatically places and breaks end crystals") {
	init {
		// Settings
		DelaySettings
		HaltSettings
		ValiditySettings
		CalculationSettings
		TargetSettings
		MiscellaneousSettings
	}

	// TODO: Not Yet Implemented
}
