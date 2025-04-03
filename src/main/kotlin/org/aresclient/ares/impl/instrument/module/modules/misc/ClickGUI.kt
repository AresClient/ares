package org.aresclient.ares.impl.instrument.module.modules.misc

import org.aresclient.ares.Ares
import org.aresclient.ares.api.instruments.Module
import org.aresclient.ares.api.util.Keys
import org.aresclient.ares.impl.gui.game.AresClickGUI

object ClickGUI: Module(Category.MISC, "ClickGUI", "Opens the Ares ClickGUI", Defaults().setBind(Keys.SEMICOLON)) {
    private val screen by lazy { AresClickGUI(settings) }

    override fun onEnable() {
        Ares.LOGGER.info("ClickGUI enabled")
        MC.setScreen(screen.getScreen())
        setEnabled(false)
    }
}