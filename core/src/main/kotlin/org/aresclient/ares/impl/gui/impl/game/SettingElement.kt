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

class SettingsGroup(private val setting: Setting<*>, columns: Int,
                    columnWidth: () -> Float, private val childHeight: Float, x: Float = 0f, y: Float = 0f,
                    private val skipEnabled: Boolean = false): DynamicElementGroup(columns, columnWidth, childHeight, x, y) {
    init {
        refresh()
    }

    fun refresh() {
        getChildren().clear()

        if (setting.type == Setting.Type.MAP) (setting as Setting.Map<*>).value.forEach { (name, setting) ->
            if(name.first() != '.' && (!skipEnabled || name != "Enabled")) pushChild(when(setting.type) {
                //Setting.Type.LIST -> CategoryElement(it, content, childHeight)
                Setting.Type.BOOLEAN -> BooleanElement(setting as Setting.Boolean, childHeight)
                Setting.Type.ENUM -> EnumElement(setting as Setting.Enum<*>, childHeight)
                Setting.Type.BIND -> BindElement(setting as Setting.Bind, childHeight)
                Setting.Type.STRING -> StringElement(setting as Setting.String, childHeight)
                Setting.Type.INTEGER -> IntElement(setting as Setting.Integer, childHeight)
                Setting.Type.LONG -> LongElement(setting as Setting.Long, childHeight)
                Setting.Type.FLOAT -> FloatElement(setting as Setting.Float, childHeight)
                Setting.Type.DOUBLE -> DoubleElement(setting as Setting.Double, childHeight)
                else -> EmptySettingElement(setting.getName(), childHeight)
            })
        }
        // TODO: LIST AND OTHER SETTINGS FULLSCREEN
        /*is Setting<*> -> if(serializable.type == Setting.Type.LIST) {
            (serializable.value as List<*>).forEach { addListElements(it, true) }
            (serializable.possibleValues as ListValues<*>).values
                .filterNot((serializable.value as List<*>)::contains).forEach { addListElements(it, false) }
        }*/
    }

    /*private fun addListElements(any: Any?, added: Boolean) {
        if(any is Enum<*>) pushChild(ListSubElement(EnumListElementAdapter(any), added, content, childHeight))
        else if(any is String) pushChild(ListSubElement(DefaultListElementAdapter(any), added, content, childHeight))
    }*/
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
    private val group = SettingsGroup(setting,  1, this::getWidth, 18f)

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

    fun refresh() {
        group.refresh()
    }

    //override fun getContentHeight(): Float = group.getHeight()
}

class EmptySettingElement(private val name: String, height: Float): SettingElement(height) {
    override fun getText(): String = name
}

abstract class SettingElement(defaultHeight: Float, private val start: Float = 3f): DynamicElement() {
    protected val fontRenderer = RenderHelper.getFontRenderer(defaultHeight * 13f/18f)

    abstract fun getText(): String
    open fun getTextColor(theme: Theme): Setting.Color = theme.lightground

    override fun draw(theme: Theme, buffers: Renderer.Buffers, matrixStack: MatrixStack, mouseX: Int, mouseY: Int, delta: Float) {
        buffers.lines.draw(matrixStack) {
            vertices(
                0f, getHeight(), 0f, 2f, theme.primary.value.red, theme.primary.value.green, theme.primary.value.blue, theme.primary.value.alpha,
                getWidth(), getHeight(), 0f, 2f, theme.primary.value.red, theme.primary.value.green, theme.primary.value.blue, theme.primary.value.alpha
            )
            indices(0, 1)
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
