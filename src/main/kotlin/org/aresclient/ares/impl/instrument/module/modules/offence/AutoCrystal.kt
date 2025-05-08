package org.aresclient.ares.impl.instrument.module.modules.offence

import org.aresclient.ares.api.instruments.Module
import org.aresclient.ares.impl.instrument.module.components.offence.autocrystal.*

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
