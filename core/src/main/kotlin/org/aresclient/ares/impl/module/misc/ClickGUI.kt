package org.aresclient.ares.impl.module.misc

import org.aresclient.ares.api.Ares
import org.aresclient.ares.api.module.Category
import org.aresclient.ares.api.module.Module
import org.aresclient.ares.impl.gui.impl.game.AresClickGUI
import org.aresclient.ares.api.util.Keys

object ClickGUI: Module(Category.MISC, "ClickGUI", "Opens the Ares ClickGUI", Defaults().setBind(Keys.SEMICOLON)) {
    private val screen by lazy { AresClickGUI(settings) }

    override fun onEnable() {
        Ares.getMinecraft().openScreen(screen.getScreen())
    }
}