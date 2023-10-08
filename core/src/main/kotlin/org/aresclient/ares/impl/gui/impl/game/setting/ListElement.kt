package org.aresclient.ares.impl.gui.impl.game.setting

import org.aresclient.ares.api.setting.Setting
import org.aresclient.ares.impl.gui.impl.game.DropDownSettingElement
import org.aresclient.ares.impl.gui.impl.game.SettingsContent
import org.aresclient.ares.impl.gui.impl.game.SettingsGroup

class ListElement(private val content: SettingsContent, setting: Setting.List<*>, private val scale: Float):
    DropDownSettingElement<Setting.List<*>>(setting, scale) {
    init {
        pushChild(SettingElementButton(this) {
            content.getWindow()?.open {
                addString("setting", setting.path)
                SettingsContent::class.java
            }
        })
        element = SettingsGroup(setting, 1, content,  settingHeight = scale * 0.87f)
    }

    override fun change() {
        (element as SettingsGroup).refresh()
    }
}