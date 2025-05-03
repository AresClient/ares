package org.aresclient.ares.impl.gui.game.setting

import org.aresclient.ares.api.setting.settings.ListSetting
import org.aresclient.ares.impl.gui.game.DropDownSettingElement
import org.aresclient.ares.impl.gui.game.SettingsContent
import org.aresclient.ares.impl.gui.game.SettingsMap

class ListElement(private val content:SettingsContent, setting: ListSetting, private val scale: Float):
	DropDownSettingElement<ListSetting>(setting, scale) {
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