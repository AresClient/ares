package org.aresclient.ares.impl.gui.game.setting

import org.aresclient.ares.api.setting.settings.list.MapListSetting
import org.aresclient.ares.impl.gui.game.DropDownSettingElement
import org.aresclient.ares.impl.gui.game.SettingsContent
import org.aresclient.ares.impl.gui.game.SettingsMap

class MapListElement(private val content: SettingsContent, setting: MapListSetting, scale: Float):
	DropDownSettingElement<MapListSetting>(setting, scale) {
    init {
        pushChild(SettingElementButton(this) {
            content.getWindow()?.open {
                addString("setting", setting.path)
                SettingsContent::class.java
            }
        })
        element = SettingsMap(setting, 1, content,  settingHeight = scale * 0.87f)
    }

    override fun change() {
        (element as SettingsMap).refresh()
    }
}