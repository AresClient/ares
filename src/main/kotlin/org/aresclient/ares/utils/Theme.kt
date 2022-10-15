package org.aresclient.ares.utils

import net.meshmc.mesh.util.render.Color
import org.aresclient.ares.Ares
import org.aresclient.ares.SColor

class Theme(_primary: SColor, _secondary: SColor, _background: SColor, _lightground: SColor) {
    companion object {
        private val SETTING = Ares.SETTINGS.category("Theme")
        private val PRIMARY = SETTING.color("Primary", Color(0.37254903f, 0.019607844f, 0.019607844f, 1f))
        private val SECONDARY = SETTING.color("Secondary", Color(0.09803922f, 0.09803922f, 0.09803922f, 1f))
        private val BACKGROUND = SETTING.color("Background", Color(0f, 0f, 0f, 0.9f))
        private val LIGHTGROUND = SETTING.color("Lightground", Color.WHITE)

        private var CURRENT = Theme(PRIMARY.value, SECONDARY.value, BACKGROUND.value, LIGHTGROUND.value)

        fun current(): Theme = CURRENT

        fun set(theme: Theme) {
            CURRENT = theme

            PRIMARY.value = theme.primary
            SECONDARY.value = theme.secondary
            BACKGROUND.value = theme.background
            LIGHTGROUND.value = theme.lightground
        }
    }

    var primary = _primary
        set(value) {
            field = value
            PRIMARY.value = value
        }
    var secondary = _secondary
        set(value) {
            field = value
            SECONDARY.value = value
        }
    var background = _background
        set(value) {
            field = value
            BACKGROUND.value = value
        }
    var lightground = _lightground
        set(value) {
            field = value
            LIGHTGROUND.value = value
        }
}
