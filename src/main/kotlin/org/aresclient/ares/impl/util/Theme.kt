package org.aresclient.ares.impl.util

import org.aresclient.ares.Ares
import org.aresclient.ares.api.nrender.font.CustomFont
import org.aresclient.ares.api.setting.settings.ColorSetting
import org.aresclient.ares.api.setting.settings.EnumSetting
import org.aresclient.ares.api.util.Color

// TODO: replace font with nfont after new renderer is completely implemented
data class Theme(val font: EnumSetting<Fonts>, val nfont: EnumSetting<CustomFont>, val primary: ColorSetting, val secondary: ColorSetting, val background: ColorSetting, val lightground: ColorSetting) {
    companion object {
        private val SETTING = Ares.getSettings().addMap("Theme")
        private val THEME = Theme(
            SETTING.addEnum("Font", Fonts.ARIAL),
            SETTING.addEnum("NFont", CustomFont.ARIAL),
            SETTING.addColor("Primary",
                Color(0.37254903f, 0.019607844f, 0.019607844f, 1f)
            ),
            SETTING.addColor("Secondary",
                Color(0.09803922f, 0.09803922f, 0.09803922f, 1f)
            ),
            SETTING.addColor("Background", Color(0f, 0f, 0f, 0.95f)),
            SETTING.addColor("Lightground", Color.WHITE)
        )

        fun current(): Theme = THEME
    }
}
