package org.aresclient.ares.impl.gui.impl.game.setting

import org.aresclient.ares.api.setting.Setting
import org.aresclient.ares.impl.gui.impl.game.*
import org.aresclient.ares.impl.util.Theme

class MapElement(private val content: WindowContent, setting: Setting.Map<*>, scale: Float):
    DropDownSettingElement<Setting.Map<*>>(setting, scale) {
    private val enabled: Setting<Boolean>? = setting.value["Enabled"] as? Setting.Boolean

    init {
        pushChild(SettingElementButton(this) {
            if(enabled != null) enabled.value = !enabled.value
            else content.getWindow()?.open {
                addString("setting", setting.path)
                SettingsContent::class.java
            }
        })
        element = SettingsGroup(setting, 1, content,  enabled != null, scale * 0.87f)
    }

    override fun getTextColor(theme: Theme): Setting.Color = if(enabled?.value == true) theme.primary else theme.lightground
}
