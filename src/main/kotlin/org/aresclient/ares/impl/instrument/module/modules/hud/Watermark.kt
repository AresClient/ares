package org.aresclient.ares.impl.instrument.module.modules.hud

import org.aresclient.ares.impl.AresPlugin

object Watermark: TextHudModule("Watermark", "Displays an Ares watermark on the hud", Defaults().setEnabled(true)) {
    override fun getText() = "Ares ${AresPlugin.version}"
}
