package org.aresclient.ares.impl.util

import org.aresclient.ares.Ares
import org.aresclient.ares.api.setting.settings.ColorSetting
import org.aresclient.ares.api.util.Color

data class Theme(val primary: ColorSetting, val secondary: ColorSetting, val background: ColorSetting, val lightground: ColorSetting) {
    companion object {
        private val SETTING = Ares.SETTINGS.addMap("Theme")
        private val THEME = Theme(
            SETTING.addColor("Primary",
                Color(0.37254903f, 0.019607844f, 0.019607844f, 1f)
            ),
            SETTING.addColor("Secondary",
                Color(0.09803922f, 0.09803922f, 0.09803922f, 1f)
            ),
            SETTING.addColor("Background", Color(0f, 0f, 0f, 0.9f)),
            SETTING.addColor("Lightground", Color.WHITE)
        )

        fun current(): Theme = THEME
    }
}
