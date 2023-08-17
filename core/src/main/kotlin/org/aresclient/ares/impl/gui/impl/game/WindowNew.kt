package org.aresclient.ares.impl.gui.impl.game

import org.aresclient.ares.api.setting.ISerializer
import org.aresclient.ares.api.setting.Setting
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
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.sqrt

private val FONT_RENDERER = RenderHelper.getFontRenderer(14f)
private const val TOP_SIZE = 18f

class WindowManager<T>(private val settings: Setting.Map<*>) {
    private val windowSettings = settings.addList<Setting.Map<*>>(Setting.Type.MAP, "Windows")
    private var windows = ArrayList<WindowElement<T>>()

    fun open(content: WindowContent<*>) {

    }
}

class WindowContent<T>(serializer: ISerializer<T>, private var title: String, private var icon: Texture): StaticElement() {
    val settings = Setting.Map(serializer)
}

class WindowSettings<T>(serializer: ISerializer<T>, data: Setting.Map<T>,
        defaultWidth: kotlin.Float = 130f, defaultHeight: kotlin.Float = 300f,
        defaultX: kotlin.Float = 0f, defaultY: kotlin.Float = 0f): Setting.Map<T>(serializer) {

}

class WindowElement<T>(serializer: ISerializer<T>, settings: Setting.Map<T>): DynamicElement() {
    //val clazz = settings.addString("clazz", "") // TODO: DEFAULT
    //private val data = settings.add(data)
    private val x = settings.addFloat("x", 0f)
    private val y = settings.addFloat("y", 0f)
    private val width = settings.addFloat("width", 130f)
    private val height = settings.addFloat("height", 300f)

    private var holding = false
    private var holdX = 0f
    private var holdY = 0f

    private val icon = Image(DEFAULT_ICON, 2f, 1f, TOP_SIZE - 2, TOP_SIZE - 2)
    private val closeButton = CloseButton({ getWidth() }) { /*context.close(this)*/ }
    private val backButton = BackButton({ closeButton.getX() }, { true }, { /*back()*/ })

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

    override fun update() {
        //currContent?.update()
        super.update()
    }

    override fun draw(theme: Theme, buffers: Renderer.Buffers, matrixStack: MatrixStack, mouseX: Int, mouseY: Int, delta: Float) {
        val width = getWidth()
        val height = getHeight()
        val topOffset = -1 + (2 * TOP_SIZE / height)
        val bottomOffset = -(height - TOP_SIZE) / height

        // move window if dragging
        if(holding) {
            val parent = getRootParent()!!
            x.value = max(0f, min(parent.getWidth() - width, mouseX.toFloat() - holdX))
            y.value = max(0f, min(parent.getHeight() - TOP_SIZE, mouseY.toFloat() - holdY))
        }

        // top window bar background
        buffers.uniforms.roundedRadius.set(0.04f)
        buffers.uniforms.roundedSize.set(width, height)
        buffers.rounded.draw(matrixStack) {
            vertices(
                width,
                TOP_SIZE, 0f, 1f, topOffset, theme.secondary.value.red, theme.secondary.value.green, theme.secondary.value.blue, theme.secondary.value.alpha,
                width, 0f, 0f, 1f, -1f, theme.secondary.value.red, theme.secondary.value.green, theme.secondary.value.blue, theme.secondary.value.alpha,
                0f,
                TOP_SIZE, 0f, -1f, topOffset, theme.secondary.value.red, theme.secondary.value.green, theme.secondary.value.blue, theme.secondary.value.alpha,
                0f, 0f, 0f, -1f, -1f, theme.secondary.value.red, theme.secondary.value.green, theme.secondary.value.blue, theme.secondary.value.alpha
            )
            indices(
                0, 1, 2,
                1, 2, 3
            )
        }

        // window title text
        /*if(!content.empty()) {
            val title = content.peek().getTitle()
            FONT_RENDERER.drawString(
                matrixStack, title,
                ((backButton.getX() - icon.getX() - icon.getWidth()) / 2f
                        + icon.getX() + icon.getWidth()) - FONT_RENDERER.getStringWidth(title) / 2f,
                1f, 1f, 1f, 1f, 1f
            )
        }*/

        // line under window top
        buffers.lines.draw(matrixStack) {
            vertices(
                0f,
                TOP_SIZE, 0f, 2f, theme.primary.value.red, theme.primary.value.green, theme.primary.value.blue, theme.primary.value.alpha,
                width,
                TOP_SIZE, 0f, 2f, theme.primary.value.red, theme.primary.value.green, theme.primary.value.blue, theme.primary.value.alpha
            )
            indices(0, 1)
        }

        // window body, with content clipped
        RenderHelper.clip({
            buffers.rounded.draw(matrixStack) {
                vertices(
                    width, height, 0f, 1f, 1f, theme.background.value.red, theme.background.value.green, theme.background.value.blue, theme.background.value.alpha,
                    width,
                    TOP_SIZE, 0f, 1f, bottomOffset, theme.background.value.red, theme.background.value.green, theme.background.value.blue, theme.background.value.alpha,
                    0f, height, 0f, -1f, 1f, theme.background.value.red, theme.background.value.green, theme.background.value.blue, theme.background.value.alpha,
                    0f,
                    TOP_SIZE, 0f, -1f, bottomOffset, theme.background.value.red, theme.background.value.green, theme.background.value.blue, theme.background.value.alpha
                )
                indices(
                    0, 1, 2,
                    1, 2, 3
                )
            }
        }) {
            //currContent?.render(theme, buffers, matrixStack, mouseX, mouseY, delta)
        }

        super.draw(theme, buffers, matrixStack, mouseX, mouseY, delta)
    }

    override fun click(mouseX: Int, mouseY: Int, mouseButton: Int, acted: AtomicBoolean) {
        val prev = acted.get()
        super.click(mouseX, mouseY, mouseButton, acted)
        //if(mouseY > getRenderY() + TOP_SIZE) currContent?.click(mouseX, mouseY, mouseButton, acted)
        val after = acted.get()

        if(mouseButton == 0 && !holding && isMouseOver(mouseX, mouseY)) {
            if(prev == after && !prev) {
                /*context.removeChild(this)
                context.pushChild(this)*/
            }

            if(mouseY <= getRenderY() + 15f && !after) {
                holding = true
                holdX = mouseX.toFloat() - getRenderX()
                holdY = mouseY.toFloat() - getRenderY()
                acted.set(true)
            }
        }
    }

    override fun release(mouseX: Int, mouseY: Int, mouseButton: Int) {
        if(mouseButton == 0) holding = false
        super.release(mouseX, mouseY, mouseButton)
        //currContent?.release(mouseX, mouseY, mouseButton)
    }

    override fun scroll(mouseX: Int, mouseY: Int, value: Double, acted: AtomicBoolean) {
        super.scroll(mouseX, mouseY, value, acted)

        if(!acted.get() && isMouseOver(mouseX, mouseY) && mouseY >= getRenderY() + TOP_SIZE) {
            /*
            currContent?.let {
                it.setY(min(Window.TOP_SIZE, max((it.getY() + value).toFloat(), getHeight() - it.getHeight() + Window.TOP_SIZE)))
            }*/
            acted.set(true)
        }
    }

    override fun type(typedChar: Char?, keyCode: Int) {
        super.type(typedChar, keyCode)
        //currContent?.type(typedChar, keyCode)
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
