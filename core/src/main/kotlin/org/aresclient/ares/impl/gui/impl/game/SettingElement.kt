package org.aresclient.ares.impl.gui.impl.game

import org.aresclient.ares.api.Ares
import org.aresclient.ares.api.instrument.module.Category
import org.aresclient.ares.impl.gui.api.Button
import org.aresclient.ares.impl.gui.api.DynamicElement
import org.aresclient.ares.impl.util.RenderHelper
import org.aresclient.ares.impl.util.RenderHelper.draw
import org.aresclient.ares.impl.util.Theme
import org.aresclient.ares.api.render.MatrixStack
import org.aresclient.ares.api.render.Renderer
import org.aresclient.ares.api.setting.Setting
import org.aresclient.ares.impl.gui.api.DynamicElementGroup
import org.aresclient.ares.impl.gui.api.ScreenElement
import org.aresclient.ares.impl.gui.impl.game.setting.*
import java.util.concurrent.atomic.AtomicBoolean

fun String.formatToPretty(): String =
    this.split('_').joinToString(separator = " ") { it.lowercase().replaceFirstChar { c -> c.uppercase() } }

class SettingsGroup(private val setting: Setting<*>, columns: Int, private val content: SettingsContent, private val skipEnabled: Boolean = false,
    private val settingHeight: Float = 18f, visible: () -> Boolean = { true }, x: () -> Float = { 0f }, y: () -> Float = { 0f },
    width: () -> Float = { 0f }, height: () -> Float = { 0f }): DynamicElementGroup(columns, visible, x, y, width, height) {

    init {
        refresh()
    }

    fun refresh() {
        getChildren().clear()
        when(setting.type) {
            Setting.Type.MAP -> (setting as Setting.Map<*>).value.forEach { (name, setting) ->
                if(/*name.first() != '.' && */(!skipEnabled || name != "Enabled"))
                    pushChild(content.createSettingElement(setting, settingHeight))
            }
            Setting.Type.COLOR -> pushChild(ColorElement.DropDown(setting as Setting.Color, settingHeight))
            Setting.Type.LIST -> (setting as Setting.List<*>).value.forEach {
                pushChild(content.createSettingElement(it, settingHeight))
            }
            else -> throw RuntimeException("Can't open setting of type ${setting.type.name} in window")
        }
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
        for(category in Category.getAll()) {
            if(category.settings == setting) {
                setIcon(category.icon)
                break
            }
        }

        // TODO: RESIZING WINDOWS?
        pushChild(group)
    }

    override fun getTitle() = setting.getName() ?: "Home"

    override fun getHeight() = group.getHeight()

    fun createSettingElement(setting: Setting<*>, settingHeight: Float = 18f): SettingElement<*> = when(setting.type) {
        Setting.Type.BOOLEAN -> BooleanElement(setting as Setting.Boolean, settingHeight)
        Setting.Type.ENUM -> EnumElement(setting as Setting.Enum<*>, settingHeight)
        Setting.Type.BIND -> BindElement(setting as Setting.Bind, settingHeight)
        Setting.Type.STRING -> StringElement(setting as Setting.String, settingHeight)
        Setting.Type.INTEGER -> IntElement(setting as Setting.Integer, settingHeight)
        Setting.Type.LONG -> LongElement(setting as Setting.Long, settingHeight)
        Setting.Type.FLOAT -> FloatElement(setting as Setting.Float, settingHeight)
        Setting.Type.DOUBLE -> DoubleElement(setting as Setting.Double, settingHeight)
        Setting.Type.COLOR -> ColorElement(this, setting as Setting.Color, settingHeight)
        Setting.Type.LIST -> ListElement(this, setting as Setting.List<*>, settingHeight)
        Setting.Type.GROUPED -> SettingElement(setting, settingHeight) // TODO
        Setting.Type.MAP -> MapElement(this, setting as Setting.Map<*>, settingHeight)
        else -> SettingElement(setting, settingHeight)
    }
}

open class SettingElement<T: Setting<*>>(protected val setting: T, scale: Float, private val start: Float = 3f): DynamicElement(height = { scale }) {
    protected val fontRenderer = RenderHelper.getFontRenderer(scale * 13f/18f)
    private var prev = setting.value

    open fun getText(): String = setting.name ?: "<null>"
    open fun getTextColor(theme: Theme): Setting.Color = theme.lightground
    open fun getSecondaryText(): String? = null

    // TODO: maybe change to Setting::addListener, would have to also removeListener on close
    open fun change() {
    }

    open fun shouldRenderTooltip(mouseX: Int, mouseY: Int): Boolean {
        return isMouseOver(mouseX, mouseY)
    }

    override fun draw(theme: Theme, buffers: Renderer.Buffers, matrixStack: MatrixStack, mouseX: Int, mouseY: Int, delta: Float) {
        if(setting.description != null && isMouseOver(mouseX, mouseY))
            (getRootParent() as? ScreenElement)?.setTooltip(*setting.description)

        // detect changes to value, then propagate to subclasses
        if(setting.value != prev) {
            change()
            prev = setting.value
        }

        // outline
        val width = getWidth()
        val height = getHeight()
        buffers.lines.draw(matrixStack) {
            vertices(
                0f, height, 0f, 2f, theme.primary.value.red, theme.primary.value.green, theme.primary.value.blue, theme.primary.value.alpha,
                width, height, 0f, 2f, theme.primary.value.red, theme.primary.value.green, theme.primary.value.blue, theme.primary.value.alpha,
                0f, 0f, 0f, 2f, theme.primary.value.red, theme.primary.value.green, theme.primary.value.blue, theme.primary.value.alpha,
                width, 0f, 0f, 2f, theme.primary.value.red, theme.primary.value.green, theme.primary.value.blue, theme.primary.value.alpha
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

        getSecondaryText()?.let {
            fontRenderer.drawString(
                matrixStack, it, getWidth() - fontRenderer.getStringWidth(it) - 2, 1f,
                theme.lightground.value.red, theme.lightground.value.green, theme.lightground.value.blue, theme.lightground.value.alpha
            )
        }

        super.draw(theme, buffers, matrixStack, mouseX, mouseY, delta)
    }

    override fun click(mouseX: Int, mouseY: Int, mouseButton: Int, acted: AtomicBoolean) {
        super.click(mouseX, mouseY, mouseButton, acted)

        if(mouseButton == 2 && !acted.get() && isMouseOver(mouseX, mouseY)) {
            setting.setDefault()
            acted.set(true)
        }
    }

    protected class SettingElementButton(private val element: SettingElement<*>, action: (Button) -> Unit): Button(0f, 0f, 0f, 0f,
        action, Clipping.SCISSOR) {
        override fun getWidth(): Float = element.getWidth()
        override fun getHeight(): Float = element.getHeight()

        override fun draw(theme: Theme, buffers: Renderer.Buffers, matrixStack: MatrixStack, mouseX: Int, mouseY: Int) {
        }
    }

    protected abstract class SettingSubButton(scale: Float, action: (Button) -> Unit, size: Float = 0.7f, clipping: Clipping = Clipping.STENCIL):
    Button(0f, scale * (1 - size) / 2f, scale * size, scale * size, action, clipping, 2) {
        private val offset = (1f - size) / 2f

        override fun getX(): Float = getParent()?.getWidth()?.let { it - getY() - getWidth()  } ?: 0f
    }

    protected abstract class SettingSubToggleButton(scale: Float): SettingSubButton(scale, {
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

private const val DROPDOWN_PADDING = 1f
abstract class DropDownSettingElement<T: Setting<*>>(setting: T, private val scale: Float): SettingElement<T>(setting, scale, scale) {
    protected var element: DynamicElement? = null
        set(value) {
            value?.setX { DROPDOWN_PADDING }
            value?.setY { scale }
            value?.setWidth { getWidth() - DROPDOWN_PADDING }
            value?.setVisible { open }
            element?.let { removeChild(it) }
            value?.let { pushChild(it) }
            field = value
        }
    protected var open = false

    override fun getHeight(): Float {
        return if(open) (element?.getHeight() ?: 0f) + scale
        else scale
    }

    override fun click(mouseX: Int, mouseY: Int, mouseButton: Int, acted: AtomicBoolean) {
        if(isMouseOver(mouseX, mouseY) && !acted.get() && mouseY <= getRenderY() + scale && (mouseButton == 1
                    || (mouseButton == 0 && mouseX <= getRenderX() + scale))) {
            open = !open
            acted.set(true)
        }

        super.click(mouseX, mouseY, mouseButton, acted)
    }

    override fun draw(theme: Theme, buffers: Renderer.Buffers, matrixStack: MatrixStack, mouseX: Int, mouseY: Int, delta: Float) {
        super.draw(theme, buffers, matrixStack, mouseX, mouseY, delta)

        val fourth = scale / 4f
        val third = scale / 3f
        val half = scale / 2f

        matrixStack.push()
        if(open) matrixStack.model().translate(half, half, 0f).rotateZ((Math.PI / 2).toFloat()).translate(-half, -half, 0f)
        buffers.lines.draw(matrixStack) {
            vertices(
                third, fourth, 0f, 1f, theme.lightground.value.red, theme.lightground.value.green, theme.lightground.value.blue, theme.lightground.value.alpha,
                scale - third, half, 0f, 2f, theme.lightground.value.red, theme.lightground.value.green, theme.lightground.value.blue, theme.lightground.value.alpha,
                third, scale - fourth, 0f, 1f, theme.lightground.value.red, theme.lightground.value.green, theme.lightground.value.blue, theme.lightground.value.alpha
            )
            indices(0, 1, 1, 2)
        }
        matrixStack.pop()

        if(open) {
            buffers.triangle.draw(matrixStack) {
                vertices(
                    0f, scale, 0f, theme.primary.value.red, theme.primary.value.green, theme.primary.value.blue, theme.primary.value.alpha,
                    DROPDOWN_PADDING, scale, 0f, theme.primary.value.red, theme.primary.value.green, theme.primary.value.blue, theme.primary.value.alpha,
                    0f, getHeight(), 0f, theme.primary.value.red, theme.primary.value.green, theme.primary.value.blue, theme.primary.value.alpha,
                    DROPDOWN_PADDING, getHeight(), 0f, theme.primary.value.red, theme.primary.value.green, theme.primary.value.blue, theme.primary.value.alpha
                )
                indices(
                    0, 1, 2,
                    1, 2, 3
                )
            }

            buffers.lines.draw(matrixStack) {
                vertices(
                    0f, scale, 0f, 2f, theme.primary.value.red, theme.primary.value.green, theme.primary.value.blue, theme.primary.value.alpha,
                    getWidth(), scale, 0f, 2f, theme.primary.value.red, theme.primary.value.green, theme.primary.value.blue, theme.primary.value.alpha
                )
                indices(0, 1)
            }
        }
    }
}
