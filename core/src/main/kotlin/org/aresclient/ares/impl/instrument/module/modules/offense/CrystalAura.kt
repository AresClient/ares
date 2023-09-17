package org.aresclient.ares.impl.instrument.module.modules.offense

import org.aresclient.ares.api.instrument.module.Category
import org.aresclient.ares.api.instrument.module.Module
import org.aresclient.ares.impl.instrument.module.components.offense.crystalaura.*

object CrystalAura: Module(Category.OFFENSE, "CrystalAura", "Automatically places and breaks end crystals") {
	init {
		// Settings
		DelaySettings
		HaltSettings
		ValiditySettings
		CalculationSettings
		TargetSettings
		MiscellaneousSettings
	}
}
