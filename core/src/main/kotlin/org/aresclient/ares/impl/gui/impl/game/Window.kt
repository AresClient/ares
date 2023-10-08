package org.aresclient.ares.impl.gui.impl.game

import org.aresclient.ares.api.Ares
import org.aresclient.ares.impl.gui.api.Button
import org.aresclient.ares.impl.gui.api.DynamicElement
import org.aresclient.ares.impl.gui.api.Image
import org.aresclient.ares.impl.gui.api.StaticElement
import org.aresclient.ares.impl.util.RenderHelper
import org.aresclient.ares.impl.util.RenderHelper.draw
import org.aresclient.ares.impl.util.Theme
import org.aresclient.ares.api.render.MatrixStack
import org.aresclient.ares.api.render.Renderer
import org.aresclient.ares.api.render.Texture
import org.aresclient.ares.api.setting.Setting
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.sqrt

private val FONT_RENDERER = RenderHelper.getFontRenderer(14f)
private const val TOP_SIZE = 18f

class WindowManager(private val settings: Setting.List<Setting.Map<*>>): StaticElement() {
    init {
        settings.value.forEach { map ->
            pushChild(WindowElement(map, this))
        }
    }

    fun <T: WindowContent> open(creator: Setting.Map<*>.() -> Class<T>?) {
        val map = Setting.Map(Ares.getSettings().serializer)
        settings.add(map)
        pushChild(WindowElement(map, this).also {
            it.open(creator)
        })
    }

    fun close(window: WindowElement) {
        settings.remove(window.settings)
        getChildren().remove(window)
    }

    fun float(window: WindowElement) {
        removeChild(window)
        pushChild(window)
    }
}

class ErrorWindowContent(settings: Setting.Map<*>): WindowContent(settings) {
    override fun getTitle() = "ERROR"
}

abstract class WindowContent(internal val settings: Setting.Map<*>): StaticElement() {
    private var icon = DEFAULT_ICON

    abstract fun getTitle(): String

    fun getIcon() = icon
    fun setIcon(icon: Texture) {
        this.icon = icon
    }

    fun getWindow() = getParent() as? WindowElement

    override fun getWidth() = getParent()?.getWidth() ?: 0f
}

class WindowElement(internal val settings: Setting.Map<*>, private val windowManager: WindowManager): DynamicElement() {
    private val content = settings.addList<Setting.Map<*>>(Setting.Type.MAP, "Content")
    private val x = settings.addFloat("x", 0f)
    private val y = settings.addFloat("y", 0f)
    private val width = settings.addFloat("width", 130f)
    private val height = settings.addFloat("height", 300f)

    private var holding = false
    private var holdX = 0f
    private var holdY = 0f

    private var window: WindowContent? = null
    private val icon = Image(DEFAULT_ICON, 2f, 1f, TOP_SIZE - 2, TOP_SIZE - 2)
    private val closeButton = CloseButton({ getWidth() }) { windowManager.close(this) }
    private val backButton = BackButton({ closeButton.getX() }, { content.value.size > 1 }, { back() })

    init {
        setX { x.value }
        setY { y.value }
        setWidth { width.value }
        setHeight { (window?.getHeight()?.let { min(it, height.value) } ?: height.value) + TOP_SIZE }

        pushChildren(
            closeButton,
            backButton,
            icon
        )

        content.value.lastOrNull()?.let { open<WindowContent>(map = it) }
    }

    private fun <T: WindowContent> open(map: Setting.Map<*>, defaults: Setting.Map<*>.() -> Class<T>? = {null}) {
        val data = map.addMap("data")
        val default = defaults(data)
        val type = map.addString("class", default?.name ?: ErrorWindowContent::class.java.name)
        if(type.value == null) return

        setWindow(Class.forName(type.value).constructors.firstOrNull()?.newInstance(data) as? WindowContent)
    }

    fun <T: WindowContent> open(defaults: Setting.Map<*>.() -> Class<T>? = {null}) {
        val map = Setting.Map(settings.serializer)
        content.add(map)
        open(map, defaults)
    }

    private fun back() {
        if(window == null || content.value.size == 1) return
        if(content.value.isNotEmpty()) content.remove(content.value.size - 1)
        setWindow(content.value.lastOrNull()?.let {
            val clazz = it.addString("class", "")
            if(clazz.value.isNullOrEmpty()) null
            else Class.forName(clazz.value)?.constructors?.get(0)?.newInstance(it.addMap("data")) as? WindowContent
        })
    }

    private fun setWindow(window: WindowContent?) {
        this.window = window?.also {
            it.setParent(this)
            it.setY(TOP_SIZE)
            icon.setTexture(it.getIcon())
        }
    }

    override fun draw(theme: Theme, buffers: Renderer.Buffers, matrixStack: MatrixStack, mouseX: Int, mouseY: Int, delta: Float) {
        val width = getWidth()
        val height = getHeight()

        // move window if dragging
        if(holding) {
            val parent = getRootParent()!!
            x.value = max(0f, min(parent.getWidth() - width, mouseX.toFloat() - holdX))
            y.value = max(0f, min(parent.getHeight() - TOP_SIZE, mouseY.toFloat() - holdY))
        }

        window?.let { window ->
            val windowHeight = window.getHeight()
            val maxHeight = this.height.value

            // fix window scroll when window height changes
            val minY = TOP_SIZE - max(0f, windowHeight - maxHeight)
            if(window.getY() < minY) window.setY(minY)

            // scroll indicator
            if(windowHeight > maxHeight) {
                val size = maxHeight * maxHeight / windowHeight
                val offset = (TOP_SIZE - window.getY()) * (maxHeight - size) / (windowHeight - maxHeight)
                buffers.lines.draw(matrixStack) {
                    vertices(
                        width, TOP_SIZE + offset, 0f, 2f, theme.lightground.value.red, theme.lightground.value.green, theme.lightground.value.blue, theme.lightground.value.alpha,
                        width, TOP_SIZE + offset + size, 0f, 2f, theme.lightground.value.red, theme.lightground.value.green, theme.lightground.value.blue, theme.lightground.value.alpha
                    )
                    indices(0, 1)
                }
            }
        }

        RenderHelper.clip({
            buffers.triangle.draw(matrixStack) {
                vertices(
                    width, height, 0f, theme.background.value.red, theme.background.value.green, theme.background.value.blue, theme.background.value.alpha,
                    width, TOP_SIZE, 0f, theme.background.value.red, theme.background.value.green, theme.background.value.blue, theme.background.value.alpha,
                    0f, height, 0f, theme.background.value.red, theme.background.value.green, theme.background.value.blue, theme.background.value.alpha,
                    0f, TOP_SIZE, 0f, theme.background.value.red, theme.background.value.green, theme.background.value.blue, theme.background.value.alpha
                )
                indices(
                    0, 1, 2,
                    1, 2, 3
                )
            }
        }){
            window?.render(theme, buffers, matrixStack, mouseX, mouseY, delta)
        }

        // title bar
        buffers.triangle.draw(matrixStack) {
            vertices(
                width, TOP_SIZE, 0f, theme.secondary.value.red, theme.secondary.value.green, theme.secondary.value.blue, theme.secondary.value.alpha,
                width, 0f, 0f, theme.secondary.value.red, theme.secondary.value.green, theme.secondary.value.blue, theme.secondary.value.alpha,
                0f, TOP_SIZE, 0f, theme.secondary.value.red, theme.secondary.value.green, theme.secondary.value.blue, theme.secondary.value.alpha,
                0f, 0f, 0f, theme.secondary.value.red, theme.secondary.value.green, theme.secondary.value.blue, theme.secondary.value.alpha,

                width + 1f, TOP_SIZE + 1f, 0f, theme.primary.value.red, theme.primary.value.green, theme.primary.value.blue, theme.primary.value.alpha,
                width + 1f, -1f, 0f, theme.primary.value.red, theme.primary.value.green, theme.primary.value.blue, theme.primary.value.alpha,
                -1f, TOP_SIZE + 1f, 0f, theme.primary.value.red, theme.primary.value.green, theme.primary.value.blue, theme.primary.value.alpha,
                -1f, -1f, 0f, theme.primary.value.red, theme.primary.value.green, theme.primary.value.blue, theme.primary.value.alpha
            )
            indices(
                4, 5, 6,
                5, 6, 7,

                0, 1, 2,
                1, 2, 3
            )
        }

        window?.getTitle()?.let {
            FONT_RENDERER.drawString(
                matrixStack, it,
                ((backButton.getX() - icon.getX() - icon.getWidth()) / 2f
                        + icon.getX() + icon.getWidth()) - FONT_RENDERER.getStringWidth(it) / 2f,
                1f, theme.lightground.value
            )
        }

        super.draw(theme, buffers, matrixStack, mouseX, mouseY, delta)
    }

    override fun click(mouseX: Int, mouseY: Int, mouseButton: Int, acted: AtomicBoolean) {
        val prev = acted.get()
        if(isMouseOver(mouseX, mouseY) && mouseY > getRenderY() + TOP_SIZE)
            window?.click(mouseX, mouseY, mouseButton, acted)
        super.click(mouseX, mouseY, mouseButton, acted)

        if(!acted.get() && isMouseOver(mouseX, mouseY)) {
            windowManager.float(this)
            if(acted.get() == prev && mouseButton == 0 && !holding && mouseY <= getRenderY() + TOP_SIZE) {
                holding = true
                holdX = mouseX.toFloat() - getRenderX()
                holdY = mouseY.toFloat() - getRenderY()
            }
            acted.set(true)
        }
    }

    override fun release(mouseX: Int, mouseY: Int, mouseButton: Int) {
        if(mouseButton == 0) holding = false
        super.release(mouseX, mouseY, mouseButton)
        window?.release(mouseX, mouseY, mouseButton)
    }

    override fun scroll(mouseX: Int, mouseY: Int, value: Double, acted: AtomicBoolean) {
        super.scroll(mouseX, mouseY, value, acted)

        if(!acted.get() && isMouseOver(mouseX, mouseY) && mouseY >= getRenderY() + TOP_SIZE) {
            window?.let {
                it.setY(min(TOP_SIZE, max((it.getY() + value).toFloat(), getHeight() - it.getHeight())))
            }
            acted.set(true)
        }
    }

    override fun type(typedChar: Char?, keyCode: Int) {
        super.type(typedChar, keyCode)
        window?.type(typedChar, keyCode)
    }

    private open class ActionButton(private val rightX: () -> Float, action: (Button) -> Unit): Button(0f, OFFSET, SIZE, SIZE, action) {
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
                    SIZE, SIZE, 0f, 1f, 1f, theme.lightground.value.red, theme.lightground.value.green, theme.lightground.value.blue, theme.lightground.value.alpha,
                    SIZE, 0f, 0f, 1f, -1f, theme.lightground.value.red, theme.lightground.value.green, theme.lightground.value.blue, theme.lightground.value.alpha,
                    0f, SIZE, 0f, -1f, 1f, theme.lightground.value.red, theme.lightground.value.green, theme.lightground.value.blue, theme.lightground.value.alpha,
                    0f, 0f, 0f, -1f, -1f, theme.lightground.value.red, theme.lightground.value.green, theme.lightground.value.blue, theme.lightground.value.alpha
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

    private class CloseButton(rightX: () -> Float, action: (Button) -> Unit): ActionButton(rightX, action) {
        override fun draw(theme: Theme, buffers: Renderer.Buffers, matrixStack: MatrixStack, mouseX: Int, mouseY: Int) {
            super.draw(theme, buffers, matrixStack, mouseX, mouseY)

            buffers.lines.draw(matrixStack) {
                vertices(
                    PADDING_CORNER, PADDING_CORNER, 0f, 2f, theme.secondary.value.red, theme.secondary.value.green, theme.secondary.value.blue, theme.secondary.value.alpha,
                    SIZE - PADDING_CORNER, SIZE - PADDING_CORNER, 0f, 2f, theme.secondary.value.red, theme.secondary.value.green, theme.secondary.value.blue, theme.secondary.value.alpha,
                    PADDING_CORNER, SIZE - PADDING_CORNER, 0f, 2f, theme.secondary.value.red, theme.secondary.value.green, theme.secondary.value.blue, theme.secondary.value.alpha,
                    SIZE - PADDING_CORNER, PADDING_CORNER, 0f, 2f, theme.secondary.value.red, theme.secondary.value.green, theme.secondary.value.blue, theme.secondary.value.alpha
                )
                indices(0, 1, 2, 3)
            }
        }
    }

    private class BackButton(rightX: () -> Float, private val possible: () -> Boolean, action: (Button) -> Unit): ActionButton(rightX, action) {
        private companion object {
            private const val X = SIZE / 2
        }

        override fun draw(theme: Theme, buffers: Renderer.Buffers, matrixStack: MatrixStack, mouseX: Int, mouseY: Int) {
            if(possible()) super.draw(theme, buffers, matrixStack, mouseX, mouseY)
            else {
                buffers.ellipse.draw(matrixStack) {
                    vertices(
                        SIZE, SIZE, 0f, 1f, 1f, theme.lightground.value.red, theme.lightground.value.green, theme.lightground.value.blue, 0.5f,
                        SIZE, 0f, 0f, 1f, -1f, theme.lightground.value.red, theme.lightground.value.green, theme.lightground.value.blue, 0.5f,
                        0f, SIZE, 0f, -1f, 1f, theme.lightground.value.red, theme.lightground.value.green, theme.lightground.value.blue, 0.5f,
                        0f, 0f, 0f, -1f, -1f, theme.lightground.value.red, theme.lightground.value.green, theme.lightground.value.blue, 0.5f
                    )
                    indices(
                        0, 1, 2,
                        1, 2, 3
                    )
                }
            }

            buffers.lines.draw(matrixStack) {
                vertices(
                    X, MID, 0f, 2f, theme.secondary.value.red, theme.secondary.value.green, theme.secondary.value.blue, theme.secondary.value.alpha,
                    SIZE - PADDING, MID, 0f, 2f, theme.secondary.value.red, theme.secondary.value.green, theme.secondary.value.blue, theme.secondary.value.alpha
                )
                indices(0, 1)
            }

            buffers.triangle.draw(matrixStack) {
                vertices(
                    PADDING, MID, 0f, theme.secondary.value.red, theme.secondary.value.green, theme.secondary.value.blue, theme.secondary.value.alpha,
                    X, PADDING, 0f, theme.secondary.value.red, theme.secondary.value.green, theme.secondary.value.blue, theme.secondary.value.alpha,
                    X, SIZE - PADDING, 0f, theme.secondary.value.red, theme.secondary.value.green, theme.secondary.value.blue, theme.secondary.value.alpha
                )
                indices(0, 1, 2)
            }
        }
    }
}
