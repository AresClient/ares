package org.aresclient.ares.impl.gui.game.window

import org.aresclient.ares.api.setting.MapSetting

class InvalidWindowContent(settings: MapSetting): WindowContent(settings) {
    override fun getTitle() = "ERROR"
}