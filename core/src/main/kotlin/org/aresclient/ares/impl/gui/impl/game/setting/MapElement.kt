package org.aresclient.ares.impl.gui.impl.game.setting

import org.aresclient.ares.api.setting.Setting
import org.aresclient.ares.impl.gui.impl.game.SettingElement

class MapElement(private val setting: Setting.Map<*>, defaultHeight: Float): SettingElement(defaultHeight) {
    override fun getText(): String {
        return "TODO: ${setting.name}"
    }
}