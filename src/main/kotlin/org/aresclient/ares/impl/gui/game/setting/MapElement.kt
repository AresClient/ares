package org.aresclient.ares.impl.gui.game.setting

import org.aresclient.ares.api.setting.Setting
import org.aresclient.ares.api.setting.MapSetting
import org.aresclient.ares.api.setting.settings.BooleanSetting
import org.aresclient.ares.api.setting.settings.ColorSetting
import org.aresclient.ares.impl.gui.game.DropDownSettingElement
import org.aresclient.ares.impl.gui.game.SettingsContent
import org.aresclient.ares.impl.gui.game.SettingsMap
import org.aresclient.ares.impl.util.Theme

class MapElement(private val content: SettingsContent, setting: MapSetting, scale: Float):
	DropDownSettingElement<MapSetting>(setting, scale) {
    private val enabled: Setting<Boolean>? = setting.value["Enabled"] as? BooleanSetting

    init {
        pushChild(SettingElementButton(this) {
            if(enabled != null) enabled.value = !enabled.value
            else content.getWindow()?.open {
                addString("setting", setting.path)
                SettingsContent::class.java
            }
        })
        element = SettingsMap(setting, 1, content,  enabled != null, scale * 0.87f)
    }

    override fun getTextColor(theme: Theme): ColorSetting = if(enabled?.value == true) theme.primary else theme.lightground
}
