package org.aresclient.ares.impl.instrument.module.modules.misc

import org.aresclient.ares.api.Ares
import org.aresclient.ares.api.instrument.module.Category
import org.aresclient.ares.api.instrument.module.Module
import org.aresclient.ares.impl.gui.impl.game.AresClickGUI
import org.aresclient.ares.api.util.Keys

object ClickGUI: Module(Category.MISC, "ClickGUI", "Opens the Ares ClickGUI", Defaults().setBind(Keys.SEMICOLON)) {
    private val screen by lazy { AresClickGUI(settings) }

    override fun onEnable() {
        Ares.getMinecraft().openScreen(screen.getScreen())
        isEnabled = false
    }
}