package org.aresclient.ares.impl.gui.impl.game

import org.aresclient.ares.api.Ares
import org.aresclient.ares.impl.gui.api.Button
import org.aresclient.ares.impl.gui.api.DynamicElement
import org.aresclient.ares.impl.util.RenderHelper
import org.aresclient.ares.impl.util.RenderHelper.draw
import org.aresclient.ares.impl.util.Theme
import org.aresclient.ares.api.render.MatrixStack
import org.aresclient.ares.api.render.Renderer
import org.aresclient.ares.api.setting.Setting
import org.aresclient.ares.impl.gui.api.DynamicElementGroup
import org.aresclient.ares.impl.gui.impl.game.setting.*

class SettingsGroup(setting: Setting<*>, columns: Int, private val content: WindowContent, private val skipEnabled: Boolean = false,
    private val settingHeight: Float = 18f, visible: () -> Boolean = { true }, x: () -> Float = { 0f }, y: () -> Float = { 0f }, width: () -> Float = { 0f },
    height: () -> Float = { 0f }): DynamicElementGroup(columns, visible, x, y, width, height) {
    init {
        if(setting.type == Setting.Type.MAP) (setting as Setting.Map<*>).value.forEach { (name, setting) ->
            if(/*name.first() != '.' && */(!skipEnabled || name != "Enabled")) {
                pushChild(when(setting.type) {
                    //Setting.Type.LIST -> CategoryElement(it, content, childHeight)
                    Setting.Type.BOOLEAN -> BooleanElement(setting as Setting.Boolean, settingHeight)
                    Setting.Type.ENUM -> EnumElement(setting as Setting.Enum<*>, settingHeight)
                    Setting.Type.BIND -> BindElement(setting as Setting.Bind, settingHeight)
                    Setting.Type.STRING -> StringElement(setting as Setting.String, settingHeight)
                    Setting.Type.INTEGER -> IntElement(setting as Setting.Integer, settingHeight)
                    Setting.Type.LONG -> LongElement(setting as Setting.Long, settingHeight)
                    Setting.Type.FLOAT -> FloatElement(setting as Setting.Float, settingHeight)
                    Setting.Type.DOUBLE -> DoubleElement(setting as Setting.Double, settingHeight)
                    Setting.Type.MAP -> MapElement(content, setting as Setting.Map<*>, settingHeight)
                    else -> EmptySettingElement(setting.getName(), settingHeight)
                })
            }
        }
        // TODO: LIST AND OTHER SETTINGS FULLSCREEN
    }
}

class SettingsContent(settings: Setting.Map<*>): WindowContent(settings) {
    private val name = settings.addString("setting", "")
    private val setting = with(name) {
        var curr: Setting<*>? = Ares.getSettings()
        val split = value.split(":")
        for(name in split) {
            curr = (when (curr?.type) {
                Setting.Type.MAP -> (curr as Setting.Map<*>).value[name]
                Setting.Type.LIST -> name.toIntOrNull()?.let { (curr as Setting.List<*>).value[it] }
                else -> null
            }) ?: break
        }
        curr ?: Ares.getSettings()
    }
    private val group = SettingsGroup(setting,  1, this, width = this::getWidth)

    init {
        // set icon if category
        /*for((ind, cat) in Module.CATEGORIES.withIndex()) {
            if(cat == serializable) {
                setIcon(Category.values()[ind].icon)
                break
            }
        }*/

        pushChild(group)
    }


    override fun getTitle(): String = setting.getName() ?: "Home"

    //override fun getContentHeight(): Float = group.getHeight()
}

class EmptySettingElement(private val name: String, height: Float): SettingElement(height) {
    override fun getText(): String = name
}

abstract class SettingElement(defaultHeight: Float, private val start: Float = 3f): DynamicElement(height = { defaultHeight }) {
    protected val fontRenderer = RenderHelper.getFontRenderer(defaultHeight * 13f/18f)

    abstract fun getText(): String
    open fun getTextColor(theme: Theme): Setting.Color = theme.lightground

    override fun draw(theme: Theme, buffers: Renderer.Buffers, matrixStack: MatrixStack, mouseX: Int, mouseY: Int, delta: Float) {
        buffers.lines.draw(matrixStack) {
            vertices(
                0f, getHeight(), 0f, 2f, theme.primary.value.red, theme.primary.value.green, theme.primary.value.blue, theme.primary.value.alpha,
                getWidth(), getHeight(), 0f, 2f, theme.primary.value.red, theme.primary.value.green, theme.primary.value.blue, theme.primary.value.alpha,
                0f, 0f, 0f, 2f, theme.primary.value.red, theme.primary.value.green, theme.primary.value.blue, theme.primary.value.alpha,
                getWidth(), 0f, 0f, 2f, theme.primary.value.red, theme.primary.value.green, theme.primary.value.blue, theme.primary.value.alpha
            )
            indices(
                0, 1,
                0, 2,
                1, 3
            )
        }

        val color = getTextColor(theme)
        fontRenderer.drawString(
            matrixStack, getText(), start, 1f,
            color.value.red, color.value.green, color.value.blue, color.value.alpha
        )

        super.draw(theme, buffers, matrixStack, mouseX, mouseY, delta)
    }

    protected fun String.formatToPretty(): String =
        this.split('_').joinToString { it.lowercase().replaceFirstChar { c -> c.uppercase() } }

    protected class SettingElementButton(private val element: SettingElement, action: (Button) -> Unit): Button(0f, 0f, 0f, 0f,
        action, Clipping.SCISSOR) {
        override fun getWidth(): Float = element.getWidth()
        override fun getHeight(): Float = element.getHeight()

        override fun draw(theme: Theme, buffers: Renderer.Buffers, matrixStack: MatrixStack, mouseX: Int, mouseY: Int) {
        }
    }

    protected abstract class SettingSubButton(height: Float, action: (Button) -> Unit, size: Float = 0.7f, clipping: Clipping = Clipping.STENCIL):
    Button(0f, height * (1 - size) / 2f, height * size, height * size, action, clipping, 2) {
        private val offset = (1f - size) / 2f

        override fun getX(): Float = getParent()?.getWidth()?.let { it - getY() - getWidth()  } ?: 0f
    }

    protected abstract class SettingSubToggleButton(height: Float): SettingSubButton(height, {
        it as SettingSubToggleButton
        it.setState(!it.getState())
    }, 0.5f) {
        abstract fun getState(): Boolean
        abstract fun setState(value: Boolean)

        override fun draw(theme: Theme, buffers: Renderer.Buffers, matrixStack: MatrixStack, mouseX: Int, mouseY: Int) {
            val size = getHeight()

            buffers.lines.draw(matrixStack) {
                vertices(
                    0f, 0f, 0f, 2f, theme.primary.value.red, theme.primary.value.green, theme.primary.value.blue, theme.primary.value.alpha,
                    size, 0f, 0f, 2f, theme.primary.value.red, theme.primary.value.green, theme.primary.value.blue, theme.primary.value.alpha,
                    size, size, 0f, 2f, theme.primary.value.red, theme.primary.value.green, theme.primary.value.blue, theme.primary.value.alpha,
                    0f, size, 0f, 2f, theme.primary.value.red, theme.primary.value.green, theme.primary.value.blue, theme.primary.value.alpha
                )
                indices(
                    0, 1,
                    1, 2,
                    2, 3,
                    3, 0
                )
            }

            if(getState()) {
                buffers.triangle.draw(matrixStack) {
                    vertices(
                        0f, 0f, 0f, theme.primary.value.red, theme.primary.value.green, theme.primary.value.blue, theme.primary.value.alpha,
                        size, 0f, 0f, theme.primary.value.red, theme.primary.value.green, theme.primary.value.blue, theme.primary.value.alpha,
                        size, size, 0f, theme.primary.value.red, theme.primary.value.green, theme.primary.value.blue, theme.primary.value.alpha,
                        0f, size, 0f, theme.primary.value.red, theme.primary.value.green, theme.primary.value.blue, theme.primary.value.alpha
                    )
                    indices(
                        0, 1, 2,
                        0, 3, 2
                    )
                }
            }
        }
    }
}
