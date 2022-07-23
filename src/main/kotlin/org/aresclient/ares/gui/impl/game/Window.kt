package org.aresclient.ares.gui.impl.game

import org.aresclient.ares.ListValues
import org.aresclient.ares.Serializable
import org.aresclient.ares.Setting
import org.aresclient.ares.Settings
import org.aresclient.ares.gui.api.BaseElementGroup
import org.aresclient.ares.gui.api.Button
import org.aresclient.ares.gui.api.DynamicElement
import org.aresclient.ares.gui.api.Image
import org.aresclient.ares.renderer.*
import org.aresclient.ares.utils.Renderer
import org.aresclient.ares.utils.Renderer.draw
import org.aresclient.ares.utils.Theme
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.sqrt

open class Window(var title: String, path: String, private val icon: Texture, width: (expanded: Boolean) -> Float,
                  private val height: (expanded: Boolean) -> Float, defaultX: Float = 0f, defaultY: Float = 0f): DynamicElement() {
    companion object {
        val SETTINGS = AresGameScreen.SETTINGS.category("windows")
        private val FONT_RENDERER = Renderer.getFontRenderer(14f)
        const val TOP_SIZE = 18f
    }

    protected val settings = SETTINGS.category(path)
    private val x = settings.float("x", defaultX)
    private val y = settings.float("y", defaultY)
    protected val expanded = settings.boolean("expanded", false)
    private val visible = settings.boolean("visible", false)

    private var holding = false
    private var holdX = 0f
    private var holdY = 0f

    private val closeButton = CloseButton({ getWidth() }) {
        visible.value = !visible.value
    }
    private val expandButton = ExpandButton({ closeButton.getX() }, { expanded.value }) {
        expanded.value = !expanded.value
    }
    private val iconImage = Image(icon, 2f, 1f, TOP_SIZE - 2, TOP_SIZE - 2)

    init {
        setX { x.value }
        setY { y.value }
        setWidth { width(expanded.value) }
        setHeight { height(expanded.value) + TOP_SIZE }
        setVisible { visible.value }

        pushChildren(
            closeButton,
            expandButton,
            iconImage
        )
    }

    open fun draw(theme: Theme, buffers: Renderer.Buffers, matrixStack: MatrixStack) {
    }

    override fun draw(theme: Theme, buffers: Renderer.Buffers, matrixStack: MatrixStack, mouseX: Int, mouseY: Int, delta: Float) =
    Renderer.scissor(getRenderX(), getRenderY(), getWidth(), getHeight()) {
        val width = getWidth()
        val height = getHeight()

        if(holding) {
            val parent = getRootParent()!!
            x.value = max(0f, min(parent.getWidth() - width, mouseX.toFloat() - holdX))
            y.value = max(0f, min(parent.getHeight() - TOP_SIZE, mouseY.toFloat() - holdY))
        }

        val maxHeight = height(true)
        val topOffset = -1 + (2 * TOP_SIZE / maxHeight)

        buffers.uniforms.roundedRadius.set(0.04f)
        buffers.uniforms.roundedSize.set(width, maxHeight)
        buffers.rounded.draw(matrixStack) {
            vertices(
                width, TOP_SIZE, 0f, 1f, topOffset, theme.secondary.red, theme.secondary.green, theme.secondary.blue, theme.secondary.alpha,
                width, 0f, 0f, 1f, -1f, theme.secondary.red, theme.secondary.green, theme.secondary.blue, theme.secondary.alpha,
                0f, TOP_SIZE, 0f, -1f, topOffset, theme.secondary.red, theme.secondary.green, theme.secondary.blue, theme.secondary.alpha,
                0f, 0f, 0f, -1f, -1f, theme.secondary.red, theme.secondary.green, theme.secondary.blue, theme.secondary.alpha
            )
            indices(
                0, 1, 2,
                1, 2, 3
            )

            if(expanded.value) {
                val bottomOffset = -(maxHeight - TOP_SIZE) / maxHeight

                vertices(
                    width, height, 0f, 1f, 1f, theme.background.red, theme.background.green, theme.background.blue, theme.background.alpha,
                    width, TOP_SIZE, 0f, 1f, bottomOffset, theme.background.red, theme.background.green, theme.background.blue, theme.background.alpha,
                    0f, height, 0f, -1f, 1f, theme.background.red, theme.background.green, theme.background.blue, theme.background.alpha,
                    0f, TOP_SIZE, 0f, -1f, bottomOffset, theme.background.red, theme.background.green, theme.background.blue, theme.background.alpha
                )
                indices(
                    4, 5, 6,
                    5, 6, 7
                )
            }
        }

        buffers.lines.draw(matrixStack) {
            vertices(
                0f, TOP_SIZE, 0f, 1f, theme.primary.red, theme.primary.green, theme.primary.blue, theme.primary.alpha,
                width, TOP_SIZE, 0f, 1f, theme.primary.red, theme.primary.green, theme.primary.blue, theme.primary.alpha
            )
            indices(0, 1)
        }

        FONT_RENDERER.drawString(
            matrixStack, title,
            ((expandButton.getX() - iconImage.getX() - iconImage.getWidth()) / 2f
                + iconImage.getX() + iconImage.getWidth()) - FONT_RENDERER.getStringWidth(title) / 2f,
            1f, 1f, 1f, 1f, 1f
        )

        draw(theme, buffers, matrixStack)

        super.draw(theme, buffers, matrixStack, mouseX, mouseY, delta)
    }

    override fun click(mouseX: Int, mouseY: Int, mouseButton: Int) {
        // TODO: MOUSE OVER STILL COUNTS ROUNDED CORNERS
        if(mouseButton == 0 && !holding && isMouseOver(mouseX, mouseY) && mouseY <= getRenderY() + 15f
            && !closeButton.isMouseOver(mouseX, mouseY) && !expandButton.isMouseOver(mouseX, mouseY)) {
            holding = true
            holdX = mouseX.toFloat() - getRenderX()
            holdY = mouseY.toFloat() - getRenderY()
        }
        super.click(mouseX, mouseY, mouseButton)
    }

    override fun release(mouseX: Int, mouseY: Int, mouseButton: Int) {
        if(mouseButton == 0) holding = false
        super.release(mouseX, mouseY, mouseButton)
    }

    fun getIcon(): Texture = icon

    fun setVisible(value: Boolean) {
        visible.value = value
    }

    private open class ActionButton(private val rightX: () -> Float, action: () -> Unit): Button(0f, OFFSET, SIZE, SIZE, action) {
        protected companion object {
            internal const val SIZE = TOP_SIZE * 0.6f
            private const val OFFSET = (1 - SIZE / TOP_SIZE) / 2f * TOP_SIZE
            internal const val PADDING = SIZE / 3.5f
            internal const val MID = SIZE / 2f
            internal val PADDING_CORNER = MID - sqrt(PADDING * PADDING / 2f) // thank you, Pythagoras
        }

        override fun getX(): Float = rightX.invoke() - SIZE - OFFSET

        override fun draw(theme: Theme, buffers: Renderer.Buffers, matrixStack: MatrixStack, mouseX: Int, mouseY: Int) {
            buffers.ellipse.draw(matrixStack) {
                vertices(
                    SIZE, SIZE, 0f, 1f, 1f, theme.lightground.red, theme.lightground.green, theme.lightground.blue, theme.lightground.alpha,
                    SIZE, 0f, 0f, 1f, -1f, theme.lightground.red, theme.lightground.green, theme.lightground.blue, theme.lightground.alpha,
                    0f, SIZE, 0f, -1f, 1f, theme.lightground.red, theme.lightground.green, theme.lightground.blue, theme.lightground.alpha,
                    0f, 0f, 0f, -1f, -1f, theme.lightground.red, theme.lightground.green, theme.lightground.blue, theme.lightground.alpha
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

    private class CloseButton(rightX: () -> Float, action: () -> Unit): ActionButton(rightX, action) {
        override fun draw(theme: Theme, buffers: Renderer.Buffers, matrixStack: MatrixStack, mouseX: Int, mouseY: Int) {
            super.draw(theme, buffers, matrixStack, mouseX, mouseY)

            buffers.lines.draw(matrixStack) {
                vertices(
                    PADDING_CORNER, PADDING_CORNER, 0f, 2f, theme.secondary.red, theme.secondary.green, theme.secondary.blue, theme.secondary.alpha,
                    SIZE - PADDING_CORNER, SIZE - PADDING_CORNER, 0f, 2f, theme.secondary.red, theme.secondary.green, theme.secondary.blue, theme.secondary.alpha,
                    PADDING_CORNER, SIZE - PADDING_CORNER, 0f, 2f, theme.secondary.red, theme.secondary.green, theme.secondary.blue, theme.secondary.alpha,
                    SIZE - PADDING_CORNER, PADDING_CORNER, 0f, 2f, theme.secondary.red, theme.secondary.green, theme.secondary.blue, theme.secondary.alpha
                )
                indices(0, 1, 2, 3)
            }
        }
    }

    private class ExpandButton(rightX: () -> Float, private val expanded: () -> Boolean, action: () -> Unit) : ActionButton(rightX, action) {
        override fun draw(theme: Theme, buffers: Renderer.Buffers, matrixStack: MatrixStack, mouseX: Int, mouseY: Int) {
            super.draw(theme, buffers, matrixStack, mouseX, mouseY)

            buffers.lines.draw(matrixStack) {
                if(expanded()) {
                    vertices(
                        PADDING, MID, 0f, 2f, theme.secondary.red, theme.secondary.green, theme.secondary.blue, theme.secondary.alpha,
                        SIZE - PADDING, MID, 0f, 2f, theme.secondary.red, theme.secondary.green, theme.secondary.blue, theme.secondary.alpha
                    )
                    indices(0, 1)
                } else {
                    vertices(
                        MID, PADDING, 0f, 2f, theme.secondary.red, theme.secondary.green, theme.secondary.blue, theme.secondary.alpha,
                        MID, SIZE - PADDING, 0f, 2f, theme.secondary.red, theme.secondary.green, theme.secondary.blue, theme.secondary.alpha,
                        PADDING, MID, 0f, 2f, theme.secondary.red, theme.secondary.green, theme.secondary.blue, theme.secondary.alpha,
                        SIZE - PADDING, MID, 0f, 2f, theme.secondary.red, theme.secondary.green, theme.secondary.blue, theme.secondary.alpha
                    )
                    indices(0, 1, 2, 3)
                }
            }
        }
    }
}

class SettingsWindow(
    val root: Settings,
    icon: Texture, width: (expanded: Boolean) -> Float,
    height: (expanded: Boolean) -> Float,
    defaultX: Float = 0f,
    defaultY: Float = 0f): Window(root.getName(), root.getPath(), icon, width, height, defaultX, defaultY)
{
    private val group = BaseElementGroup(visible = expanded::value, y = Window::TOP_SIZE, childWidth = this::getWidth, childHeight = { SettingElement.HEIGHT })
    var displayed: Serializable = root
    // TODO: displayed group scrolling

    init {
        pushChild(group)

        root.map.values.forEach {
            if(!it.getName().first().isLowerCase())
                group.pushChild(SettingElement.makeSettingElement(it, this))
        }
    }

    fun nextPage(serializable: Serializable) {
        if(displayed !is Settings) throw java.lang.Exception("CANNOT PAGE FROM NON CATEGORY DISPLAY")
        if(!(displayed as Settings).map.containsValue(serializable)) throw java.lang.Exception("CANNOT PAGE FROM CATEGORY ${displayed.getPath()} TO ${serializable.getPath()}: NOT DERIVATIVE")

        displayed = serializable
        if(displayed is Settings) refreshCategory()
        if(displayed is Setting<*>) refreshSetting()
    }

    fun backPage() {
        if(displayed == root) return

        displayed = displayed.getParent()!!
        refreshCategory()
    }

    private fun refreshCategory() {
        if(displayed !is Settings) return

        group.getChildren().clear()

        if(displayed != root) group.pushChild(SettingElement.BackButton(this))

        (displayed as Settings).map.values.forEach {
            if(!it.getName().first().isLowerCase())
                group.pushChild(SettingElement.makeSettingElement(it, this))
        }

        title = root.getName() + displayed.getPath().substringAfter(root.getPath())
    }

    fun refreshSetting() {
        when((displayed as Setting<*>).type) {
            Setting.Type.LIST -> {
                group.getChildren().clear()

                group.pushChild(SettingElement.BackButton(this))

                ((displayed as Setting<*>).value as List<*>).forEach {
                    if(it is Enum<*>) group.pushChild(SettingElement.makeListSubElement(it.name, true, this))
                    else if(it is String) group.pushChild(SettingElement.makeListSubElement(it, true, this))
                }

                ((displayed as Setting<*>).possibleValues as ListValues<*>).values.filterNot((((displayed as Setting<*>).value) as List<*>)::contains).forEach {
                    if(it is Enum<*>) group.pushChild(SettingElement.makeListSubElement(it.name, false, this))
                    else if(it is String) group.pushChild(SettingElement.makeListSubElement(it, false, this))
                }
            }
            else -> TODO("Is it necessary to do any of the others?")
        }
    }
}
