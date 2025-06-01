package org.aresclient.ares.impl.instrument.module.modules.hud

import net.minecraft.text.Text
import org.aresclient.ares.impl.AresPlugin

object Watermark: TextHudModule("Watermark", "Displays an Ares watermark on the hud", Defaults().setEnabled(true)) {
    private val text by lazy { Text.literal("Ares ${AresPlugin.version}") }

    override fun getText(): Text {
        return text
    }
}
