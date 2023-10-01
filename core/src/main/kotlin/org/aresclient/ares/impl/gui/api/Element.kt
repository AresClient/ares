package org.aresclient.ares.impl.gui.api

import org.aresclient.ares.api.minecraft.render.Screen
import org.aresclient.ares.api.render.MatrixStack
import org.aresclient.ares.api.render.Renderer
import org.aresclient.ares.api.util.Color
import org.aresclient.ares.impl.util.RenderHelper
import org.aresclient.ares.impl.util.RenderHelper.draw
import org.aresclient.ares.impl.util.Theme
import java.util.*
import java.util.concurrent.atomic.AtomicBoolean

abstract class Element {
    private val children = Stack<Element>()
    private var parent: Element? = null

    abstract fun isVisible(): Boolean

    abstract fun getX(): Float
    abstract fun getY(): Float

    abstract fun getWidth(): Float
    abstract fun getHeight(): Float

    open fun getRenderX(): Float = getX() + (getParent()?.getRenderX() ?: 0f)
    open fun getRenderY(): Float = getY() + (getParent()?.getRenderY() ?: 0f)

    fun getChildren(): Stack<Element> = children

    open fun pushChild(child: Element): Element {
        getChildren().push(child).setParent(this)
        child.update()
        return this
    }

    fun pushChildren(vararg children: Element): Element {
        children.forEach { pushChild(it) }
        return this
    }
    fun pushChildren(children: Iterable<Element>): Element {
        children.forEach { pushChild(it) }
        return this
    }
    fun removeChild(element: Element): Element {
        getChildren().remove(element)
        return this
    }
    open fun popChild(): Element = getChildren().pop().setParent(null)

    fun getParent(): Element? = parent
    fun setParent(parent: Element?): Element {
        this.parent = parent
        return this
    }

    fun getRootParent(): Element? {
        var element = this.getParent()
        while(true) element = element?.getParent() ?: return element
    }

    fun isMouseOver(mouseX: Int, mouseY: Int): Boolean = isMouseOver(mouseX.toFloat(), mouseY.toFloat())
    open fun isMouseOver(mouseX: Float, mouseY: Float): Boolean =
        mouseX >= getRenderX()
                && mouseX <= getRenderX() + getWidth()
                && mouseY >= getRenderY()
                && mouseY <= getRenderY() + getHeight()

    open fun update() { // on window resize
        getChildren().forEach(Element::update)
    }

    // render should be called, draw should be overridden
    protected open fun draw(theme: Theme, buffers: Renderer.Buffers, matrixStack: MatrixStack, mouseX: Int, mouseY: Int, delta: Float) {
        getChildren().forEach {
            if(it.isVisible()) it.render(theme, buffers, matrixStack, mouseX, mouseY, delta)
        }
    }

    fun render(theme: Theme, buffers: Renderer.Buffers, matrixStack: MatrixStack, mouseX: Int, mouseY: Int, delta: Float) {
        matrixStack.push()
        matrixStack.model().translate(getX(), getY(), 0f)
        draw(theme, buffers, matrixStack, mouseX, mouseY, delta)
        matrixStack.pop()
    }

    // we have to make new arraylist because children may be mutated on click or release
    // acted: mutated by child elements when click has been handled or acted upon
    open fun click(mouseX: Int, mouseY: Int, mouseButton: Int, acted: AtomicBoolean) {
        getChildren().reversed().forEach {
            if(it.isVisible()) it.click(mouseX, mouseY, mouseButton, acted)
        }
    }

    open fun release(mouseX: Int, mouseY: Int, mouseButton: Int) {
        ArrayList(getChildren()).forEach {
            if(it.isVisible()) it.release(mouseX, mouseY, mouseButton)
        }
    }

    open fun type(typedChar: Char?, keyCode: Int) {
        getChildren().forEach {
            if(it.isVisible()) it.type(typedChar, keyCode)
        }
    }

    open fun scroll(mouseX: Int, mouseY: Int, value: Double, acted: AtomicBoolean) {
        val children = getChildren()
        for(i in (children.size - 1) downTo 0) { // reverse because rendering flips order on screen
            val child = children[i]
            if(child.isVisible()) child.scroll(mouseX, mouseY, value, acted)
        }
    }

    open fun close() {
        getChildren().forEach(Element::close)
    }
}

open class ScreenElement(title: String): Element() {
    private var open = false
    private val matrixStack = MatrixStack()

    private var tooltip: Array<out String>? = null
    private var prevMouseX = 0
    private var prevMouseY = 0
    private var mouseTime = 0f

    private val screen = object: Screen(title) {
        override fun update() {
            open = true
            matrixStack.projection().setOrtho(0F, width.toFloat(), height.toFloat(), 0F, 0F, 1F)
            this@ScreenElement.update()
        }

        override fun close() {
            this@ScreenElement.close()
            open = false
        }

        override fun render(mouseX: Int, mouseY: Int, delta: Float) {
            if(mouseX == prevMouseX && mouseY == prevMouseY) mouseTime += delta
            else {
                prevMouseX = mouseX
                prevMouseY = mouseY
                mouseTime = 0f
            }

            // render children
            val state = Renderer.begin2d()
            val theme = Theme.current()
            this@ScreenElement.draw(theme, state.buffers, matrixStack, mouseX, mouseY, delta) // we don't need to push matrices for screen drawing

            // draw tooltip
            if(tooltip?.isNotEmpty() == true && mouseTime > 10f) {
                matrixStack.push()
                matrixStack.model().translate(mouseX.toFloat(), mouseY.toFloat(), 0f)

                val padding = 2f
                val fontRenderer = RenderHelper.getFontRenderer(10f)
                val width = tooltip!!.maxOf { fontRenderer.getStringWidth(it) } + padding * 2
                val height = tooltip!!.size * fontRenderer.charHeight + padding * 2

                state.buffers.triangle.draw(matrixStack) {
                    vertices(
                        0f, -height, 0f, theme.background.value.red, theme.background.value.green, theme.background.value.blue, theme.background.value.alpha,
                        width, 0f, 0f, theme.background.value.red, theme.background.value.green, theme.background.value.blue, theme.background.value.alpha,
                        width, -height, 0f, theme.background.value.red, theme.background.value.green, theme.background.value.blue, theme.background.value.alpha,
                        0f, 0f, 0f, theme.background.value.red, theme.background.value.green, theme.background.value.blue, theme.background.value.alpha
                    )
                    indices(
                        0, 1, 2,
                        0, 1, 3
                    )
                }

                state.buffers.lines.draw(matrixStack) {
                    vertices(
                        0f, -height, 0f, 1f, theme.primary.value.red, theme.primary.value.green, theme.primary.value.blue, theme.primary.value.alpha,
                        width, -height, 0f, 1f, theme.primary.value.red, theme.primary.value.green, theme.primary.value.blue, theme.primary.value.alpha,
                        width, 0f, 0f, 1f, theme.primary.value.red, theme.primary.value.green, theme.primary.value.blue, theme.primary.value.alpha,
                        0f, 0f, 0f, 1f, theme.primary.value.red, theme.primary.value.green, theme.primary.value.blue, theme.primary.value.alpha
                    )
                    indices(0, 1, 1, 2, 2, 3, 3, 0)
                }

                fontRenderer.bindTexture()
                state.buffers.triangleTexColor.draw(matrixStack) {
                    for((i, line) in tooltip!!.withIndex()) {
                        fontRenderer.drawString(this, line,
                            padding, padding - height + i * fontRenderer.charHeight, Color.WHITE)
                    }
                }

                matrixStack.pop()
                tooltip = null
            }

            Renderer.end(state) // cleanup
            super.render(mouseX, mouseY, delta)
        }

        override fun click(mouseX: Int, mouseY: Int, mouseButton: Int) {
            this@ScreenElement.click(mouseX, mouseY, mouseButton, AtomicBoolean(false))
            super.click(mouseX, mouseY, mouseButton)
        }

        override fun release(mouseX: Int, mouseY: Int, mouseButton: Int) {
            this@ScreenElement.release(mouseX, mouseY, mouseButton)
            super.release(mouseX, mouseY, mouseButton)
        }

        override fun type(typedChar: Char?, keyCode: Int) {
            this@ScreenElement.type(typedChar, keyCode)
            super.type(typedChar, keyCode)
        }

        override fun scroll(mouseX: Int, mouseY: Int, value: Double) {
            this@ScreenElement.scroll(mouseX, mouseY, value, AtomicBoolean(false))
            super.scroll(mouseX, mouseY, value)
        }

        override fun shouldPause(): Boolean = false
    }

    override fun isVisible(): Boolean = open

    override fun getX(): Float = 0f
    override fun getY(): Float = 0f

    override fun getWidth(): Float = screen.width.toFloat()
    override fun getHeight(): Float = screen.height.toFloat()

    fun getScreen(): Screen = screen

    fun setTooltip(vararg tooltip: String) {
        this.tooltip = tooltip
    }
}

// an element with constantly changing dimensions or position
open class DynamicElement(
    private var visible: () -> Boolean = { true },
    private var x: () -> Float = { 0f },
    private var y: () -> Float = { 0f },
    private var width: () -> Float = { 0f },
    private var height: () -> Float = { 0f }
): Element() {
    override fun isVisible(): Boolean = visible.invoke()
    override fun getX(): Float = x.invoke()
    override fun getY(): Float = y.invoke()
    override fun getWidth(): Float = width.invoke()
    override fun getHeight(): Float = height.invoke()

    fun setVisible(value: () -> Boolean): DynamicElement {
        visible = value
        return this
    }

    fun setX(value: () -> Float): DynamicElement {
        x = value
        return this
    }

    fun setY(value: () -> Float): DynamicElement {
        y = value
        return this
    }

    fun setWidth(value: () -> Float): DynamicElement {
        width = value
        return this
    }

    fun setHeight(value: () -> Float): DynamicElement {
        height = value
        return this
    }
 }


// an element that doesn't change dimension or size
open class StaticElement(
    private var x: Float = 0f,
    private var y: Float = 0f,
    private var width: Float = 0f,
    private var height: Float = 0f
): Element() {
    private var visible = true

    override fun isVisible(): Boolean = visible
    override fun getX(): Float = x
    override fun getY(): Float = y
    override fun getWidth(): Float = width
    override fun getHeight(): Float = height

    fun setVisible(value: Boolean): StaticElement {
        visible = value
        return this
    }

    fun setX(value: Float): StaticElement {
        x = value
        return this
    }

    fun setY(value: Float): StaticElement {
        y = value
        return this
    }

    fun setWidth(value: Float): StaticElement {
        width = value
        return this
    }

    fun setHeight(value: Float): StaticElement {
        height = value
        return this
    }
}

open class DynamicElementGroup(private val columns: Int,
   visible: () -> Boolean = { true }, x: () -> Float = { 0f }, y: () -> Float = { 0f },
   width: () -> Float = { 0f }, height: () -> Float = { 0f }): DynamicElement(visible, x, y, width, height) {
    init {
        if(columns < 1) throw RuntimeException("Fewer than 1 columns in DynamicElementGroup is not possible")
    }

    override fun pushChild(child: Element): Element {
        if(child !is DynamicElement) throw RuntimeException("Cannot add static element to dynamic group!")

        val curr = getChildren().size
        val column = curr % columns

        child.setX { column * getColumnWidth() }
        child.setWidth(this::getColumnWidth)

        if(curr >= columns) {
            val prev = getChildren()[curr - columns]
            child.setY { prev.getY() + prev.getHeight() }
        } else child.setY { 0f }

        return super.pushChild(child)
    }

    override fun getHeight(): Float {
        return if(getChildren().size > 0) getChildren().peek().let { it.getY() + it.getHeight() } else 0f
    }

    private fun getColumnWidth() = getWidth() / columns
}
