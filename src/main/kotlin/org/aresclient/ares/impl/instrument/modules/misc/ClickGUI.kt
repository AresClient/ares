package org.aresclient.ares.impl.instrument.modules.misc

import org.aresclient.ares.api.instruments.Module
import org.aresclient.ares.impl.gui.game.AresClickGUI
import org.lwjgl.glfw.GLFW

object ClickGUI: Module(Category.MISC, "ClickGUI", "Opens the Ares ClickGUI", Defaults().setBind(GLFW.GLFW_KEY_SEMICOLON).setExternalModuleList(false)) {
    private val screen by lazy { AresClickGUI(settings) }

    override fun onEnable() {
        MC.setScreen(screen.getScreen())
        setEnabled(false)
    }
}