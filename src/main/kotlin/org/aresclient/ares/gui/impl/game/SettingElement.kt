package org.aresclient.ares.gui.impl.game

import net.meshmc.mesh.util.render.Color
import org.aresclient.ares.ListValues
import org.aresclient.ares.Serializable
import org.aresclient.ares.Setting
import org.aresclient.ares.Settings
import org.aresclient.ares.gui.api.Button
import org.aresclient.ares.gui.api.DynamicElement
import org.aresclient.ares.renderer.MatrixStack
import org.aresclient.ares.utils.Renderer
import org.aresclient.ares.utils.Renderer.draw
import org.aresclient.ares.utils.Theme
import kotlin.math.pow
import kotlin.math.sqrt

abstract class SettingDynamicElement(val string: String, val window: SettingsWindow): DynamicElement() {
    override fun draw(theme: Theme, buffers: Renderer.Buffers, matrixStack: MatrixStack, mouseX: Int, mouseY: Int, delta: Float) {
        buffers.lines.draw(matrixStack) {
            vertices(
                0f,
                SettingElement.HEIGHT, 0f, 1f, theme.primary.red, theme.primary.green, theme.primary.blue, theme.primary.alpha,
                getWidth(),
                SettingElement.HEIGHT, 0f, 1f, theme.primary.red, theme.primary.green, theme.primary.blue, theme.primary.alpha
            )
            indices(
                0, 1
            )
        }

        SettingElement.FONT_RENDERER.drawString(
            matrixStack, string,
            1f, 1f, 1f, 1f, 1f, 1f
        )

        super.draw(theme, buffers, matrixStack, mouseX, mouseY, delta)
    }
}

open class SettingElement(serializable: Serializable, window: SettingsWindow): SettingDynamicElement(serializable.getName(), window) {
    companion object {
        val FONT_RENDERER = Renderer.getFontRenderer(12f)
        const val HEIGHT = 16f

        fun makeSettingElement(serializable: Serializable, window: SettingsWindow): SettingElement =
            if(serializable is Settings && serializable.getPath() != "/gui/windows") CategoryElement(serializable, window)
            else if(serializable is Setting<*> && serializable.type == Setting.Type.LIST) ListElement(serializable as Setting<List<*>>, window)
            else SettingElement(serializable, window)

        fun makeListSubElement(name: String, added: Boolean, window: SettingsWindow): DynamicElement =
            ListElement.ListSubElement(name, added, window)
    }

    protected abstract class SettingButton(protected val rightX: () -> Float, action: () -> Unit): Button(0f, OFFSET, SIZE, SIZE, action) {
        protected companion object {
            internal const val SIZE = HEIGHT * 0.6f
            private const val OFFSET = (1 - SIZE / HEIGHT) / 2f * HEIGHT
            internal const val PADDING = SIZE / 3.5f
            internal const val MID = SIZE / 2f
            internal val PADDING_CORNER = MID - sqrt(PADDING * PADDING / 2f) // thank you, Pythagoras
        }

        override fun getX(): Float = rightX.invoke() - SIZE - OFFSET

        override fun draw(theme: Theme, buffers: Renderer.Buffers, matrixStack: MatrixStack, mouseX: Int, mouseY: Int) {
            if(getToggledState()) circle(buffers, matrixStack, theme.lightground)
            else if(isMouseOver(mouseX, mouseY)) circle(buffers, matrixStack, theme.secondary)
            else circle(buffers, matrixStack, theme.primary)
        }

        private fun circle(buffers: Renderer.Buffers, matrixStack: MatrixStack, color: Color) {
            buffers.ellipse.draw(matrixStack) {
                vertices(
                    SIZE, SIZE, 0f, 1f, 1f, color.red, color.green, color.blue, color.alpha,
                    SIZE, 0f, 0f, 1f, -1f, color.red, color.green, color.blue, color.alpha,
                    0f, SIZE, 0f, -1f, 1f, color.red, color.green, color.blue, color.alpha,
                    0f, 0f, 0f, -1f, -1f, color.red, color.green, color.blue, color.alpha
                )
                indices(
                    0, 1, 2,
                    1, 2, 3
                )
            }
        }

        open fun getToggledState() = false

        override fun isMouseOver(mouseX: Float, mouseY: Float): Boolean {
            val halfW = getWidth() / 2f
            val halfH = getHeight() / 2f
            return (mouseX - getRenderX() - halfW).pow(2) / halfW.pow(2) + (mouseY - getRenderY() - halfH).pow(2) / halfH.pow(2) <= 1
        }
    }

    private class PageButton(rightX: () -> Float, setting: Serializable, window: SettingsWindow): SettingButton(rightX, { window.nextPage(setting) }) {
        override fun draw(theme: Theme, buffers: Renderer.Buffers, matrixStack: MatrixStack, mouseX: Int, mouseY: Int) {
            super.draw(theme, buffers, matrixStack, mouseX, mouseY)
            arrow(buffers, matrixStack, theme.lightground)
        }

        private fun arrow(buffers: Renderer.Buffers, matrixStack: MatrixStack, color: Color) {
            buffers.lines.draw(matrixStack) {
                vertices(
                    PADDING_CORNER, PADDING_CORNER, 0f, 2f, color.red, color.green, color.blue, color.alpha,
                    SIZE - PADDING_CORNER, SIZE / 2, 0f, 2f, color.red, color.green, color.blue, color.alpha,
                    PADDING_CORNER, SIZE - PADDING_CORNER, 0f, 2f, color.red, color.green, color.blue, color.alpha
                )
                indices(0,1, 1,2)
            }
        }
    }

    private class CategoryElement(settings: Settings, window: SettingsWindow): SettingElement(settings, window) {
        private val pageButton = PageButton(this::getWidth, settings, window)
        private val windowButton = WindowButton({ getWidth() - pageButton.getWidth() }, settings)

        init {
            pushChildren(
                pageButton,
                windowButton
            )
        }


        private class WindowButton(rightX: () -> Float, val settings: Settings):
            SettingButton(rightX, { settings.window!!.setVisible(!settings.window!!.isVisible()) })
        {
            override fun draw(theme: Theme, buffers: Renderer.Buffers, matrixStack: MatrixStack, mouseX: Int, mouseY: Int) {
                super.draw(theme, buffers, matrixStack, mouseX, mouseY)

                if(getToggledState()) cross(buffers, matrixStack, theme.secondary)
                else if(isMouseOver(mouseX, mouseY)) square(buffers, matrixStack, theme.lightground)
                else square(buffers, matrixStack, theme.lightground)
            }

            private fun square(buffers: Renderer.Buffers, matrixStack: MatrixStack, color: Color) {
                buffers.lines.draw(matrixStack) {
                    vertices(
                        PADDING_CORNER, PADDING_CORNER, 0f, 2f, color.red, color.green, color.blue, color.alpha,
                        SIZE - PADDING_CORNER, SIZE - PADDING_CORNER, 0f, 2f, color.red, color.green, color.blue, color.alpha,
                        PADDING_CORNER, SIZE - PADDING_CORNER, 0f, 2f, color.red, color.green, color.blue, color.alpha,
                        SIZE - PADDING_CORNER, PADDING_CORNER, 0f, 2f, color.red, color.green, color.blue, color.alpha
                    )
                    indices(
                        0,2, 2,1,
                        1,3, 3,0
                    )
                }
            }

            private fun cross(buffers: Renderer.Buffers, matrixStack: MatrixStack, color: Color) {
                buffers.lines.draw(matrixStack) {
                    vertices(
                        PADDING_CORNER, PADDING_CORNER, 0f, 2f, color.red, color.green, color.blue, color.alpha,
                        SIZE - PADDING_CORNER, SIZE - PADDING_CORNER, 0f, 2f, color.red, color.green, color.blue, color.alpha,
                        PADDING_CORNER, SIZE - PADDING_CORNER, 0f, 2f, color.red, color.green, color.blue, color.alpha,
                        SIZE - PADDING_CORNER, PADDING_CORNER, 0f, 2f, color.red, color.green, color.blue, color.alpha
                    )
                    indices(0,1, 2,3)
                }
            }

            override fun getToggledState(): Boolean = settings.window?.isVisible() == true
        }
    }

    private class ListElement(setting: Setting<List<*>>, window: SettingsWindow): SettingElement(setting, window) {
        private val pageButton = PageButton(this::getWidth, setting, window)

        init {
            pushChild(pageButton)
        }

        class ListSubElement(string: String, added: Boolean, window: SettingsWindow):
            SettingDynamicElement(
                run {
                    var str = ""
                    var lastIsSpace = true
                    for(char in string) {
                        if(char == '_') {
                            str += " "
                            lastIsSpace = true
                        } else {
                            str +=
                                if (lastIsSpace) char.uppercase()
                                else char.lowercase()
                            lastIsSpace = false
                        }
                    }
                    str
                }, window) {
            val actionButton = if(added) RemoveButton(this::getWidth, string, window) else AddButton(this::getWidth, string, window)

            init {
                pushChild(actionButton)
            }

            private class AddButton(rightX: () -> Float, name: String, window: SettingsWindow): SettingButton(rightX, {
                val newList = ((window.displayed as Setting<*>).value as List<*>).toMutableList()

                if((((window.displayed as Setting<*>).possibleValues as ListValues<*>).values)[0] is Enum<*>)
                        ((((window.displayed as Setting<*>).possibleValues as ListValues<*>).values)[0] as Enum<*>).let {
                            it.javaClass.enumConstants.forEach { if(it.name == name) newList.add(it) }
                        }

                else newList.add(name)

                (window.displayed as Setting<List<*>>).value = newList
                window.refreshSetting()
            }) {
                override fun draw(theme: Theme, buffers: Renderer.Buffers, matrixStack: MatrixStack, mouseX: Int, mouseY: Int) {
                    super.draw(theme, buffers, matrixStack, mouseX, mouseY)

                    buffers.lines.draw(matrixStack) {
                        vertices(
                            MID, PADDING, 0f, 2f, theme.lightground.red, theme.lightground.green, theme.lightground.blue, theme.lightground.alpha,
                            MID, SIZE - PADDING, 0f, 2f, theme.lightground.red, theme.lightground.green, theme.lightground.blue, theme.lightground.alpha,
                            PADDING, MID, 0f, 2f, theme.lightground.red, theme.lightground.green, theme.lightground.blue, theme.lightground.alpha,
                            SIZE - PADDING, MID, 0f, 2f, theme.lightground.red, theme.lightground.green, theme.lightground.blue, theme.lightground.alpha
                        )
                        indices(0, 1, 2, 3)
                    }
                }
            }

            private class RemoveButton(rightX: () -> Float, name: String, window: SettingsWindow): SettingButton(rightX, {
                val newList = ((window.displayed as Setting<*>).value as List<*>).toMutableList()

                if((((window.displayed as Setting<*>).possibleValues as ListValues<*>).values)[0] is Enum<*>)
                    ((((window.displayed as Setting<*>).possibleValues as ListValues<*>).values)[0] as Enum<*>).let {
                        it.javaClass.enumConstants.forEach { if(it.name == name) newList.remove(it) }
                    }

                else newList.remove(name)

                (window.displayed as Setting<List<*>>).value = newList
                window.refreshSetting()
            }) {
                override fun draw(theme: Theme, buffers: Renderer.Buffers, matrixStack: MatrixStack, mouseX: Int, mouseY: Int) {
                    super.draw(theme, buffers, matrixStack, mouseX, mouseY)

                    buffers.lines.draw(matrixStack) {
                        vertices(
                            PADDING, MID, 0f, 2f, theme.lightground.red, theme.lightground.green, theme.lightground.blue, theme.lightground.alpha,
                            SIZE - PADDING, MID, 0f, 2f, theme.lightground.red, theme.lightground.green, theme.lightground.blue, theme.lightground.alpha
                        )
                        indices(0, 1)
                    }
                }
            }
        }
    }

    class BackButton(window: SettingsWindow): SettingDynamicElement("<= Back", window) {
        override fun click(mouseX: Int, mouseY: Int, mouseButton: Int) {
            if(isMouseOver(mouseX, mouseY)) window.backPage()
            super.click(mouseX, mouseY, mouseButton)
        }
    }
}