package org.aresclient.ares.gui.impl.game

import org.aresclient.ares.*
import org.aresclient.ares.gui.api.BaseElementGroup
import org.aresclient.ares.gui.api.Button
import org.aresclient.ares.gui.api.Element
import org.aresclient.ares.gui.impl.game.setting.*
import org.aresclient.ares.module.Category
import org.aresclient.ares.module.Module
import org.aresclient.ares.renderer.MatrixStack
import org.aresclient.ares.utils.Renderer
import org.aresclient.ares.utils.Renderer.draw
import org.aresclient.ares.utils.Theme

private val FONT_RENDERER = Renderer.getFontRenderer(13f)
private const val HEIGHT = 18f

class SettingsContent(settings: Settings): WindowContent(settings) {
    // TODO: CLEAN UP ElementGroup
    private val group = BaseElementGroup(childWidth = this::getWidth, childHeight = { HEIGHT })
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

    @Suppress("UNCHECKED_CAST")
    fun refresh() {
        group.getChildren().clear()

        when(serializable) {
            is Settings -> serializable.getMap().values.forEach {
                if(it.getName().first() != '.') group.pushChild(when(it) {
                    is Settings -> CategoryElement(it, this)
                    is Setting<*> -> when(it.type) {
                        Setting.Type.LIST -> CategoryElement(it, this)
                        Setting.Type.BOOLEAN -> BooleanElement(it as Setting<Boolean>)
                        Setting.Type.ENUM -> EnumElement(it as Setting<Enum<*>>)
                        Setting.Type.BIND -> BindElement(it as Setting<Int>)
                        Setting.Type.STRING -> StringElement(it as Setting<String>)
                        else -> SettingElement(it.getName()) {}
                    }
                    else -> SettingElement(it.getName()) {}
                })
            }
            is Setting<*> -> if(serializable.type == Setting.Type.LIST) {
                (serializable.value as List<*>).forEach { addListElements(it, true) }
                (serializable.possibleValues as ListValues<*>).values
                    .filterNot((serializable.value as List<*>)::contains).forEach { addListElements(it, false) }
            }
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun addListElements(any: Any?, added: Boolean) {
        if(any is Enum<*>) group.pushChild(ListSubElement(EnumListElementAdapter(any), added, this))
        else if(any is String) group.pushChild(ListSubElement(DefaultListElementAdapter(any), added, this))
    }

    fun forward(name: String): SettingsContent =
        SettingsContent(Settings.new().also { it.string("setting", "${setting.value}:$name") })

    override fun getContentHeight(): Float = group.getHeight()
}

open class SettingElement(private val text: String, action: (Button) -> Unit):
    Button(0f, 0f, 0f, 0f, action, Clipping.SCISSOR) {
    protected val fontRenderer = FONT_RENDERER

    override fun draw(theme: Theme, buffers: Renderer.Buffers, matrixStack: MatrixStack, mouseX: Int, mouseY: Int) {
        buffers.lines.draw(matrixStack) {
            vertices(
                0f, getHeight(), 0f, 1f, theme.primary.red, theme.primary.green, theme.primary.blue, theme.primary.alpha,
                getWidth(), getHeight(), 0f, 1f, theme.primary.red, theme.primary.green, theme.primary.blue, theme.primary.alpha
            )
            indices(
                0, 1
            )
        }

        FONT_RENDERER.drawString(
            matrixStack, text,
            3f, 1f,
            theme.lightground.red, theme.lightground.green, theme.lightground.blue, theme.lightground.alpha
        )
    }

    protected fun String.formatToPretty(): String =
        this.split('_').joinToString { it.lowercase().replaceFirstChar { c -> c.uppercase() } }
}

abstract class SettingSubButton(action: (Button) -> Unit, private val size: Float = 0.7f, clipping: Clipping = Clipping.STENCIL):
    Button(0f, 0f, 0f, 0f, action, clipping, 2) {
    private val offset = (1f - size) / 2f

    override fun getX(): Float = getParent()?.getWidth()?.let { it - getY() - getWidth()  } ?: 0f

    override fun getY(): Float = getParent()?.getHeight()?.let { it * offset } ?: 0f

    override fun getHeight(): Float = getParent()?.getHeight()?.let { it * size } ?: 0f

    override fun draw(theme: Theme, buffers: Renderer.Buffers, matrixStack: MatrixStack, mouseX: Int, mouseY: Int) {
        if(holding) matrixStack.model().translate(0f, 1f, 0f)
    }
}

abstract class SettingSubToggleButton: SettingSubButton({
    it as SettingSubToggleButton
    it.setState(!it.getState())
}, 0.5f) {
    abstract fun getState(): Boolean
    abstract fun setState(value: Boolean)

    override fun getWidth(): Float = getHeight()

    override fun draw(theme: Theme, buffers: Renderer.Buffers, matrixStack: MatrixStack, mouseX: Int, mouseY: Int) {
        super.draw(theme, buffers, matrixStack, mouseX, mouseY)

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
