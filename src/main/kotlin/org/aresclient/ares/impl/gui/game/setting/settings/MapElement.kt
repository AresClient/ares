package org.aresclient.ares.impl.gui.game.setting.settings

import org.aresclient.ares.api.setting.MapSetting
import org.aresclient.ares.api.setting.Setting
import org.aresclient.ares.api.setting.settings.BooleanSetting
import org.aresclient.ares.api.util.Color
import org.aresclient.ares.impl.gui.game.setting.DropdownSettingContainer
import org.aresclient.ares.impl.gui.game.setting.RowSettingGroup
import org.aresclient.ares.impl.gui.game.setting.SettingsWindowContent
import org.aresclient.ares.impl.util.Theme

class MapElement(private val content: SettingsWindowContent, setting: MapSetting, scale: Float):
	DropdownSettingContainer<MapSetting, Map<String, Setting<*>>>(setting, scale) {
    private val enabled: Setting<Boolean>? = setting.value["Enabled"] as? BooleanSetting

    init {
        pushChild(RowButton(this) {
            if(enabled != null) enabled.value = !enabled.value
            else content.getWindow()?.open {
                addString("setting", setting.path)
                SettingsWindowContent::class.java
            }
        })
        setDropdown(RowSettingGroup(setting, 1, content, scale * 0.87f))
    }

    override fun getTextColor(theme: Theme): Color = if(enabled?.value == true) theme.primary.value else theme.lightground.value
}
