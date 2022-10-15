package org.aresclient.ares.gui.impl.game

import net.meshmc.mesh.util.render.Color
import org.aresclient.ares.*
import org.aresclient.ares.gui.api.Button
import org.aresclient.ares.gui.api.DynamicElement
import org.aresclient.ares.gui.api.DynamicElementGroup
import org.aresclient.ares.gui.impl.game.setting.*
import org.aresclient.ares.module.Category
import org.aresclient.ares.module.Module
import org.aresclient.ares.renderer.MatrixStack
import org.aresclient.ares.utils.Renderer
import org.aresclient.ares.utils.Renderer.draw
import org.aresclient.ares.utils.Theme

class SettingsGroup(private val serializable: Serializable, private val content: SettingsContent, columns: Int,
                    columnWidth: () -> Float, private val childHeight: Float, x: Float = 0f, y: Float = 0f,
                    private val skipEnabled: Boolean = false): DynamicElementGroup(columns, columnWidth, childHeight, x, y) {
    init {
        refresh()
    }

    @Suppress("UNCHECKED_CAST")
    fun refresh() {
        getChildren().clear()

        when(serializable) {
            is Settings -> serializable.getMap().values.forEach {
                if(it.getName().first() != '.' && (!skipEnabled || it.getName() != "Enabled")) pushChild(when(it) {
                    is Settings -> CategoryElement(it, content, childHeight)
                    is Setting<*> -> when(it.type) {
                        Setting.Type.LIST -> CategoryElement(it, content, childHeight)
                        Setting.Type.BOOLEAN -> BooleanElement(it as Setting<Boolean>, childHeight)
                        Setting.Type.ENUM -> EnumElement(it as Setting<Enum<*>>, childHeight)
                        Setting.Type.BIND -> BindElement(it as Setting<Int>, childHeight)
                        Setting.Type.STRING -> StringElement(it as Setting<String>, childHeight)
                        Setting.Type.INTEGER -> IntElement(it as Setting<Int>, childHeight)
                        Setting.Type.LONG -> LongElement(it as Setting<Long>, childHeight)
                        Setting.Type.FLOAT -> FloatElement(it as Setting<Float>, childHeight)
                        Setting.Type.DOUBLE -> DoubleElement(it as Setting<Double>, childHeight)
                        else -> EmptySettingElement(it.getName(), childHeight)
                    }
                    else -> EmptySettingElement(it.getName(), childHeight)
                })
            }
            is Setting<*> -> if(serializable.type == Setting.Type.LIST) {
                (serializable.value as List<*>).forEach { addListElements(it, true) }
                (serializable.possibleValues as ListValues<*>).values
                    .filterNot((serializable.value as List<*>)::contains).forEach { addListElements(it, false) }
            }
        }
    }

    private fun addListElements(any: Any?, added: Boolean) {
        if(any is Enum<*>) pushChild(ListSubElement(EnumListElementAdapter(any), added, content, childHeight))
        else if(any is String) pushChild(ListSubElement(DefaultListElementAdapter(any), added, content, childHeight))
    }
}

class SettingsContent(settings: Settings): WindowContent(settings) {
    companion object {
        fun Window.open(serializable: Serializable) {
            open(SettingsContent(Settings.new().also { it.string("setting", serializable.getFullName()) }))
        }

        fun WindowContent.open(serializable: Serializable) {
            open(SettingsContent(Settings.new().also { it.string("setting", serializable.getFullName()) }))
        }
    }

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
    private val group = SettingsGroup(serializable, this, 1, this::getWidth, 18f)

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
    }

    fun getSerializable(): Serializable = serializable

    fun getPath(): String = setting.value

    fun refresh() {
        group.refresh()
    }

    override fun getContentHeight(): Float = group.getHeight()
}

class EmptySettingElement(private val name: String, height: Float): SettingElement(height) {
    override fun getText(): String = name
}

abstract class SettingElement(protected val defaultHeight: Float, private val start: Float = 3f): DynamicElement() {
    protected val fontRenderer = Renderer.getFontRenderer(defaultHeight * 13f/18f)

    abstract fun getText(): String
    open fun getTextColor(theme: Theme): Color = theme.lightground

    override fun draw(theme: Theme, buffers: Renderer.Buffers, matrixStack: MatrixStack, mouseX: Int, mouseY: Int, delta: Float) {
        buffers.lines.draw(matrixStack) {
            vertices(
                0f, getHeight(), 0f, 2f, theme.primary.red, theme.primary.green, theme.primary.blue, theme.primary.alpha,
                getWidth(), getHeight(), 0f, 2f, theme.primary.red, theme.primary.green, theme.primary.blue, theme.primary.alpha
            )
            indices(0, 1)
        }

        val color = getTextColor(theme)
        fontRenderer.drawString(
            matrixStack, getText(), start, 1f,
            color.red, color.green, color.blue, color.alpha
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
                    0f, 0f, 0f, 2f, theme.primary.red, theme.primary.green, theme.primary.blue, theme.primary.alpha,
                    size, 0f, 0f, 2f, theme.primary.red, theme.primary.green, theme.primary.blue, theme.primary.alpha,
                    size, size, 0f, 2f, theme.primary.red, theme.primary.green, theme.primary.blue, theme.primary.alpha,
                    0f, size, 0f, 2f, theme.primary.red, theme.primary.green, theme.primary.blue, theme.primary.alpha
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
                        0f, 0f, 0f, theme.primary.red, theme.primary.green, theme.primary.blue, theme.primary.alpha,
                        size, 0f, 0f, theme.primary.red, theme.primary.green, theme.primary.blue, theme.primary.alpha,
                        size, size, 0f, theme.primary.red, theme.primary.green, theme.primary.blue, theme.primary.alpha,
                        0f, size, 0f, theme.primary.red, theme.primary.green, theme.primary.blue, theme.primary.alpha
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
