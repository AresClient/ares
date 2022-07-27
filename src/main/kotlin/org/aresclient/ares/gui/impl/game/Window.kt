package org.aresclient.ares.gui.impl.game

import org.aresclient.ares.*
import org.aresclient.ares.gui.api.*
import org.aresclient.ares.renderer.*
import org.aresclient.ares.utils.Renderer
import org.aresclient.ares.utils.Renderer.draw
import org.aresclient.ares.utils.Theme
import java.util.Stack
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.sqrt

open class WindowContent(private val settings: Settings): StaticElement(0f, Window.TOP_SIZE, 0f, 0f) {
    private var window: Window? = null
    private var icon = Window.DEFAULT_ICON
    private var title = ""

    override fun getWidth(): Float = getParent()?.getWidth() ?: 0f
    override fun getHeight(): Float = (getParent()?.getHeight() ?: getY()) - getY()

    fun open(content: WindowContent) {
        val parent = getParent()
        if(parent is Window) parent.open(content)
    }

    fun getSettings(): Settings = settings

    fun getWindow(): Window? = window
    fun setWindow(window: Window?) {
        this.window = window
    }

    fun getIcon(): Texture = icon
    protected fun setIcon(texture: Texture) {
        icon = texture
    }

    fun getTitle(): String = title
    protected fun setTitle(string: String) {
        title = string
    }
}

open class WindowContext(rootSettings: Settings, title: String): ScreenElement(title) {
    private val settings = rootSettings.array("windows")
    private val windows = settings.value.map { Window(this, it) }.toMutableList()
    private val listeners = arrayListOf<() -> Unit>()

    override fun init() {
        windows.forEach { pushChild(it) }
        listeners.forEach { it.invoke() }
    }

    fun open(window: Window) {
        windows.add(window)
        pushChild(window)
        settings.value.add(window.getSettings())
        listeners.forEach { it.invoke() }
    }

    fun close(window: Window) {
        windows.remove(window)
        removeChild(window)
        settings.value.remove(window.getSettings())
        listeners.forEach { it.invoke() }
    }

    override fun click(mouseX: Int, mouseY: Int, mouseButton: Int) {
        for(element in getChildren().reversed()) {
            if(!element.isVisible()) continue
            else if(element.isMouseOver(mouseX, mouseY)) {
                if(element is Window) {
                    removeChild(element)
                    pushChild(element)
                }
                element.click(mouseX, mouseY, mouseButton)
                return
            }
        }
    }

    fun getWindows(): List<Window> = windows
    fun getListeners(): ArrayList<() -> Unit> = listeners
}

// TODO: RESIZABLE WINDOWS?
class Window(private val context: WindowContext, private val settings: Settings = Settings.new(),
         defaultWidth: Float = 130f, defaultHeight: Float = 300f, defaultX: Float = 0f, defaultY: Float = 0f): DynamicElement() {
    companion object {
        private val FONT_RENDERER = Renderer.getFontRenderer(14f)
        internal const val TOP_SIZE = 18f
        val DEFAULT_ICON = Texture(Ares::class.java.getResourceAsStream("/assets/ares/textures/icons/gears.png"))
    }

    private val x = settings.float("x", defaultX)
    private val y = settings.float("y", defaultY)
    private val width = settings.float("width", defaultWidth)
    private val height = settings.float("height", defaultHeight)

    private var holding = false
    private var holdX = 0f
    private var holdY = 0f

    private val icon = Image(DEFAULT_ICON, 2f, 1f, TOP_SIZE - 2, TOP_SIZE - 2)
    private val closeButton = CloseButton({ getWidth() }) { context.close(this) }
    private val backButton = BackButton({ closeButton.getX() }, this::back)

    private var contentSetting = settings.array("content")
    private var content = Stack<WindowContent>().also {
        it.addAll(contentSetting.value.mapNotNull {
            val clazz = it.string("class", "")
            if(clazz.value.isEmpty()) null
            else Class.forName(clazz.value).getConstructor(Settings::class.java).newInstance(it.category("data")) as WindowContent
        })
        if(!it.empty()) it.peek().also { curr ->
            curr.setWindow(this)
            icon.setTexture(curr.getIcon())
            pushChild(curr)
        }
    }

    init {
        setX { x.value }
        setY { y.value }
        setWidth { width.value }
        setHeight { height.value + TOP_SIZE }

        pushChildren(
            closeButton,
            backButton,
            icon
        )
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

        val topOffset = -1 + (2 * TOP_SIZE / height)
        val bottomOffset = -(height - TOP_SIZE) / height

        buffers.uniforms.roundedRadius.set(0.04f)
        buffers.uniforms.roundedSize.set(width, height)
        buffers.rounded.draw(matrixStack) {
            vertices(
                width, TOP_SIZE, 0f, 1f, topOffset, theme.secondary.red, theme.secondary.green, theme.secondary.blue, theme.secondary.alpha,
                width, 0f, 0f, 1f, -1f, theme.secondary.red, theme.secondary.green, theme.secondary.blue, theme.secondary.alpha,
                0f, TOP_SIZE, 0f, -1f, topOffset, theme.secondary.red, theme.secondary.green, theme.secondary.blue, theme.secondary.alpha,
                0f, 0f, 0f, -1f, -1f, theme.secondary.red, theme.secondary.green, theme.secondary.blue, theme.secondary.alpha,

                width, height, 0f, 1f, 1f, theme.background.red, theme.background.green, theme.background.blue, theme.background.alpha,
                width, TOP_SIZE, 0f, 1f, bottomOffset, theme.background.red, theme.background.green, theme.background.blue, theme.background.alpha,
                0f, height, 0f, -1f, 1f, theme.background.red, theme.background.green, theme.background.blue, theme.background.alpha,
                0f, TOP_SIZE, 0f, -1f, bottomOffset, theme.background.red, theme.background.green, theme.background.blue, theme.background.alpha
            )
            indices(
                0, 1, 2,
                1, 2, 3,
                4, 5, 6,
                5, 6, 7
            )
        }

        buffers.lines.draw(matrixStack) {
            vertices(
                0f, TOP_SIZE, 0f, 1f, theme.primary.red, theme.primary.green, theme.primary.blue, theme.primary.alpha,
                width, TOP_SIZE, 0f, 1f, theme.primary.red, theme.primary.green, theme.primary.blue, theme.primary.alpha
            )
            indices(0, 1)
        }

        if(!content.empty()) {
            val title = content.peek().getTitle()
            FONT_RENDERER.drawString(
                matrixStack, title,
                ((backButton.getX() - icon.getX() - icon.getWidth()) / 2f
                        + icon.getX() + icon.getWidth()) - FONT_RENDERER.getStringWidth(title) / 2f,
                1f, 1f, 1f, 1f, 1f
            )
        }

        super.draw(theme, buffers, matrixStack, mouseX, mouseY, delta)
    }

    override fun click(mouseX: Int, mouseY: Int, mouseButton: Int) {
        if(mouseButton == 0 && !holding && isMouseOver(mouseX, mouseY) && mouseY <= getRenderY() + 15f
            && !closeButton.isMouseOver(mouseX, mouseY) && !backButton.isMouseOver(mouseX, mouseY)) {
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

    fun getSettings(): Settings = settings

    fun getCurrentContent(): WindowContent? = if(!content.empty()) content.peek() else null

    fun open(windowContent: WindowContent) {
        windowContent.setWindow(this)

        if(!content.empty()) removeChild(content.peek())
        pushChild(windowContent)
        icon.setTexture(windowContent.getIcon())

        content.push(windowContent)
        contentSetting.value.add(Settings.new().also {
            it.string("class", windowContent.javaClass.name)
            it.getMap()["data"] = windowContent.getSettings()
        })

        context.getListeners().forEach { it.invoke() }
    }

    fun back() {
        if(content.size < 2) return

        content.pop()?.let {
            it.setWindow(null)
            removeChild(it)
        }
        contentSetting.value.removeLast()

        if(!content.empty()) content.peek()?.let { content ->
            pushChild(content)
            icon.setTexture(content.getIcon())
        }

        context.getListeners().forEach { it.invoke() }
    }

    fun duplicate(): Window = Window(context, settings.clone()).also {
        it.x.value += (if(it.x.value >= context.getWidth() - getWidth() * 2 - 2) -1 else 1) * getWidth() + 2
        context.open(it)
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

    private class BackButton(rightX: () -> Float, action: () -> Unit): ActionButton(rightX, action) {
        private companion object {
            private const val X = SIZE / 2
        }

        override fun draw(theme: Theme, buffers: Renderer.Buffers, matrixStack: MatrixStack, mouseX: Int, mouseY: Int) {
            super.draw(theme, buffers, matrixStack, mouseX, mouseY)

            buffers.lines.draw(matrixStack) {
                vertices(
                    X, MID, 0f, 2f, theme.secondary.red, theme.secondary.green, theme.secondary.blue, theme.secondary.alpha,
                    SIZE - PADDING, MID, 0f, 2f, theme.secondary.red, theme.secondary.green, theme.secondary.blue, theme.secondary.alpha
                )
                indices(0, 1)
            }

            buffers.triangle.draw(matrixStack) {
                vertices(
                    PADDING, MID, 0f, theme.secondary.red, theme.secondary.green, theme.secondary.blue, theme.secondary.alpha,
                    X, PADDING, 0f, theme.secondary.red, theme.secondary.green, theme.secondary.blue, theme.secondary.alpha,
                    X, SIZE - PADDING, 0f, theme.secondary.red, theme.secondary.green, theme.secondary.blue, theme.secondary.alpha
                )
                indices(0, 1, 2)
            }
        }
    }
}
