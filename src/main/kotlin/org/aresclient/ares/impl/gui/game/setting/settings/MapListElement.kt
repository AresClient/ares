package org.aresclient.ares.impl.gui.game.setting.settings

import org.aresclient.ares.api.setting.MapSetting
import org.aresclient.ares.api.setting.settings.list.MapListSetting
import org.aresclient.ares.impl.gui.game.setting.DropdownSettingContainer
import org.aresclient.ares.impl.gui.game.setting.RowSettingGroup
import org.aresclient.ares.impl.gui.game.setting.SettingsWindowContent

class MapListElement(private val content: SettingsWindowContent, setting: MapListSetting, scale: Float):
	DropdownSettingContainer<MapListSetting, List<MapSetting>>(setting, scale) {
    init {
        pushChild(RowButton(this) {
            content.getWindow()?.open {
                addString("setting", setting.path)
                SettingsWindowContent::class.java
            }
        })
        setDropdown(RowSettingGroup(setting, 1, content, settingHeight = scale * 0.87f))
    }

    override fun change() {
        (getDropdown() as RowSettingGroup).refresh()
    }
}
