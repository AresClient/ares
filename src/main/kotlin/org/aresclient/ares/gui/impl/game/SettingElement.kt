package org.aresclient.ares.gui.impl.game

import net.meshmc.mesh.util.render.Color
import org.aresclient.ares.*
import org.aresclient.ares.gui.api.BaseElementGroup
import org.aresclient.ares.gui.api.Button
import org.aresclient.ares.gui.api.DynamicElement
import org.aresclient.ares.module.Category
import org.aresclient.ares.module.Module
import org.aresclient.ares.renderer.MatrixStack
import org.aresclient.ares.utils.Renderer
import org.aresclient.ares.utils.Renderer.draw
import org.aresclient.ares.utils.Theme
import kotlin.math.pow
import kotlin.math.sqrt

class SettingsContent(settings: Settings): WindowContent(settings) {
    // TODO: CLEAN UP ElementGroup
    private val group = BaseElementGroup(childWidth = this::getWidth, childHeight = { SettingElement.HEIGHT })
    private val setting = settings.string("setting", "")
    private val serializable = setting.let {
        var serializable: Serializable = Ares.SETTINGS
        val split = it.value.split(":")
        for(name in split) {
            if(serializable is Settings) serializable = serializable.getMap()[name] ?: continue
            else break
        }
        serializable
    }

    init {
        setTitle(serializable.getName())

        // set icon if category
        for((ind, cat) in Module.CATEGORIES.withIndex()) {
            if(cat == serializable) {
                setIcon(Category.values()[ind].icon)
                break
            }
        }

        pushChild(group)
        refresh()
    }

    fun getSerializable(): Serializable = serializable

    fun getPath(): String = setting.value

    fun refresh() {
        group.getChildren().clear()

        when(serializable) {
            is Settings -> serializable.getMap().values.forEach {
                // TODO: better way of checking if setting should be hidden? eg, . b4 hidden settings
                if(!it.getName().first().isLowerCase()) group.pushChild(SettingElement.makeSettingElement(it, this))
            }
            is Setting<*> -> if(serializable.type == Setting.Type.LIST) {
                (serializable.value as List<*>).forEach { add(it, true) }
                (serializable.possibleValues as ListValues<*>).values.filterNot((serializable.value as List<*>)::contains).forEach { add(it, false) }
            }
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun add(any: Any?, added: Boolean) {
        if(any is Enum<*>) group.pushChild(SettingElement.makeListSubElement(any.name, added, this))
        else if(any is String) group.pushChild(SettingElement.makeListSubElement(any, added, this))
    }

    fun forward(name: String): SettingsContent =
        SettingsContent(Settings.new().also { it.string("setting", "${setting.value}:$name") })
}

abstract class SettingDynamicElement(val string: String, val content: SettingsContent): DynamicElement() {
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

open class SettingElement(serializable: Serializable, content: SettingsContent): SettingDynamicElement(serializable.getName(), content) {
    companion object {
        val FONT_RENDERER = Renderer.getFontRenderer(12f)
        const val HEIGHT = 16f

        fun makeSettingElement(serializable: Serializable, content: SettingsContent): SettingElement =
            if(serializable is Settings ||(serializable is Setting<*> && serializable.type == Setting.Type.LIST))
                CategoryElement(serializable, content)
            else SettingElement(serializable, content)

        fun makeListSubElement(name: String, added: Boolean, content: SettingsContent): DynamicElement =
            ListElement.ListSubElement(name, added, content)
    }

    protected abstract class SettingButton(private val rightX: () -> Float, action: () -> Unit): Button(0f, OFFSET, SIZE, SIZE, action) {
        protected companion object {
            internal const val SIZE = HEIGHT * 0.6f
            private const val OFFSET = (1 - SIZE / HEIGHT) / 2f * HEIGHT
            internal const val PADDING = SIZE / 3.5f
            internal const val MID = SIZE / 2f
            internal val PADDING_CORNER = MID - sqrt(PADDING * PADDING / 2f) // thank you, Pythagoras
        }

        override fun getX(): Float = rightX.invoke() - SIZE - OFFSET

        override fun draw(theme: Theme, buffers: Renderer.Buffers, matrixStack: MatrixStack, mouseX: Int, mouseY: Int) {
            if(isMouseOver(mouseX, mouseY)) circle(buffers, matrixStack, theme.secondary)
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

        override fun isMouseOver(mouseX: Float, mouseY: Float): Boolean {
            val halfW = getWidth() / 2f
            val halfH = getHeight() / 2f
            return (mouseX - getRenderX() - halfW).pow(2) / halfW.pow(2) + (mouseY - getRenderY() - halfH).pow(2) / halfH.pow(2) <= 1
        }
    }

    private class PageButton(rightX: () -> Float, serializable: Serializable, content: SettingsContent):
        SettingButton(rightX, { content.open(content.forward(serializable.getName())) }) {
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

    private class CategoryElement(serializable: Serializable, content: SettingsContent): SettingElement(serializable, content) {
        private val pageButton = PageButton(this::getWidth, serializable, this.content)
        private val windowButton = WindowButton({ getWidth() - pageButton.getWidth() }, serializable, content)

        init {
            pushChildren(
                pageButton,
                windowButton
            )
        }

        private class WindowButton(rightX: () -> Float, serializable: Serializable, content: SettingsContent):
            SettingButton(rightX, { content.getWindow()?.duplicate()?.open(content.forward(serializable.getName())) }) // OPEN NEW WINDOW OF SETTINGS
        {
            override fun draw(theme: Theme, buffers: Renderer.Buffers, matrixStack: MatrixStack, mouseX: Int, mouseY: Int) {
                super.draw(theme, buffers, matrixStack, mouseX, mouseY)

                if(isMouseOver(mouseX, mouseY)) square(buffers, matrixStack, theme.lightground)
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
        }
    }

    private class ListElement(setting: Setting<List<*>>, content: SettingsContent): SettingElement(setting, content) {
        private val pageButton = PageButton(this::getWidth, setting, content)

        init {
            pushChild(pageButton)
        }

        class ListSubElement(string: String, added: Boolean, content: SettingsContent):
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
                }, content) {
            val actionButton = if(added) RemoveButton(this::getWidth, string, content) else AddButton(this::getWidth, string, content)

            init {
                pushChild(actionButton)
            }

            @Suppress("UNCHECKED_CAST")
            private class AddButton(rightX: () -> Float, name: String, content: SettingsContent): SettingButton(rightX, {
                val serializable = content.getSerializable() as Setting<ArrayList<Any>>
                val first = (serializable.possibleValues as ListValues<*>).values[0]
                if(first is Enum<*>) first.javaClass.enumConstants.forEach { if(it.name == name) serializable.value.add(it) }
                else serializable.value.add(name)

                content.refresh()
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

            @Suppress("UNCHECKED_CAST")
            private class RemoveButton(rightX: () -> Float, name: String, content: SettingsContent): SettingButton(rightX, {
                val serializable = content.getSerializable() as Setting<ArrayList<Any>>
                val first = (serializable.possibleValues as ListValues<*>).values[0]
                if(first is Enum<*>) first.javaClass.enumConstants.forEach { if(it.name == name) serializable.value.remove(it) }
                else serializable.value.remove(name)

                content.refresh()
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
}
