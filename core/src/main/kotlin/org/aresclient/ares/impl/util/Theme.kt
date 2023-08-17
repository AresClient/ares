package org.aresclient.ares.impl.util

import org.aresclient.ares.api.setting.Setting
import org.aresclient.ares.impl.AresPlugin
import org.aresclient.ares.api.util.Color

data class Theme(val primary: Setting.Color<*>, val secondary: Setting.Color<*>, val background: Setting.Color<*>, val lightground: Setting.Color<*>) {
    companion object {
        private val SETTING = AresPlugin.INSTANCE.settings.addMap("Theme")
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
