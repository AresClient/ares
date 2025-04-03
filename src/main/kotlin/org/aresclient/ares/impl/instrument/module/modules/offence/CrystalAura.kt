package org.aresclient.ares.impl.instrument.module.modules.offence

import org.aresclient.ares.api.instruments.Module
import org.aresclient.ares.impl.instrument.module.components.offence.crystalaura.*

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
