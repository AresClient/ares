package org.aresclient.ares.impl.instrument.modules.misc

import org.aresclient.ares.api.instruments.Module
import org.aresclient.ares.api.util.Keys
import org.aresclient.ares.impl.gui.game.AresClickGUI

object ClickGUI: Module(Category.MISC, "ClickGUI", "Opens the Ares ClickGUI", Defaults().setBind(Keys.SEMICOLON).setExternalModuleList(false)) {
    private val screen by lazy { AresClickGUI(settings) }

    override fun onEnable() {
        MC.setScreen(screen.getScreen())
        setEnabled(false)
    }
}