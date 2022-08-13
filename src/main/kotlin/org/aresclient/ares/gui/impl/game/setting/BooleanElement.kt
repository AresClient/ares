package org.aresclient.ares.gui.impl.game.setting

import net.meshmc.mesh.util.render.Color
import org.aresclient.ares.Setting
import org.aresclient.ares.gui.impl.game.SettingElement
import org.aresclient.ares.gui.impl.game.SettingSubButton
import org.aresclient.ares.renderer.MatrixStack
import org.aresclient.ares.utils.Renderer
import org.aresclient.ares.utils.Renderer.draw
import org.aresclient.ares.utils.Theme
import kotlin.math.max
import kotlin.math.min

class BooleanElement(private val setting: Setting<Boolean>): SettingElement(setting.getName(), {
    (it as BooleanElement).toggle()
}) {
    private var last = 0L
    private var anim = false

    init {
        pushChild(ToggleButton(this))
    }

    private fun toggle() {
        setting.value = !setting.value
        anim = true
        last = System.currentTimeMillis()
    }

    private class ToggleButton(private val element: BooleanElement): SettingSubButton({ element.toggle() }) {
        override fun getWidth(): Float = getHeight() * 2

        override fun draw(theme: Theme, buffers: Renderer.Buffers, matrixStack: MatrixStack, mouseX: Int, mouseY: Int) {
            val width = getWidth()
            val mid = width / 2f
            val quarter = mid / 2f
            val height = getHeight()
            val color = if(element.setting.value) Color.GREEN else Color.RED

            buffers.ellipse.draw(matrixStack) {
                vertices(
                    mid, height, 0f, 1f, 1f, color.red, color.green, color.blue, color.alpha,
                    mid, 0f, 0f, 1f, -1f, color.red, color.green, color.blue, color.alpha,
                    0f, height, 0f, -1f, 1f, color.red, color.green, color.blue, color.alpha,
                    0f, 0f, 0f, -1f, -1f, color.red, color.green, color.blue, color.alpha,
                    width, height, 0f, -1f, 1f, color.red, color.green, color.blue, color.alpha,
                    width, 0f, 0f, -1f, -1f, color.red, color.green, color.blue, color.alpha,

                    quarter, height, 0f, 0f, 1f, color.red, color.green, color.blue, color.alpha,
                    quarter, 0f, 0f, 0f, -1f, color.red, color.green, color.blue, color.alpha,
                    width - quarter, height, 0f, 0f, 1f, color.red, color.green, color.blue, color.alpha,
                    width - quarter, 0f, 0f, 0f, -1f, color.red, color.green, color.blue, color.alpha
                )
                indices(
                    0, 1, 2,
                    1, 2, 3,
                    4, 5, 0,
                    5, 0, 1,

                    6, 7, 8,
                    7, 8, 9
                )
            }

            val x = if(element.anim) {
                val frac = (System.currentTimeMillis() - element.last) / 200f
                if(frac >= 1) element.anim = false

                val offset = max(0f, min(height, frac * height))
                if(element.setting.value) offset
                else height - offset
            } else if(!element.setting.value) 0f
            else height

            buffers.ellipse.draw(matrixStack) {
                vertices(
                    x + height, height, 0f, 1f, 1f, theme.lightground.red, theme.lightground.green, theme.lightground.blue, theme.lightground.alpha,
                    x + height, 0f, 0f, 1f, -1f, theme.lightground.red, theme.lightground.green, theme.lightground.blue, theme.lightground.alpha,
                    x, height, 0f, -1f, 1f, theme.lightground.red, theme.lightground.green, theme.lightground.blue, theme.lightground.alpha,
                    x, 0f, 0f, -1f, -1f, theme.lightground.red, theme.lightground.green, theme.lightground.blue, theme.lightground.alpha
                )
                indices(
                    0, 1, 2,
                    1, 2, 3
                )
            }
        }
    }
}