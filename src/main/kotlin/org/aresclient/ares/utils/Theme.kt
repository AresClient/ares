package org.aresclient.ares.utils

import net.meshmc.mesh.util.render.Color
import org.aresclient.ares.Ares

class Theme(_primary: Color, _secondary: Color, _background: Color, _lightground: Color) {
    companion object {
        private val SETTING = Ares.SETTINGS.category("theme")
        private val PRIMARY = SETTING.color("primary", Color(0.37254903f, 0.019607844f, 0.019607844f, 1f))
        private val SECONDARY = SETTING.color("secondary", Color(0.09803922f, 0.09803922f, 0.09803922f, 1f))
        private val BACKGROUND = SETTING.color("background", Color(0f, 0f, 0f, 0.8f))
        private val LIGHTGROUND = SETTING.color("lightground", Color.WHITE)

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
