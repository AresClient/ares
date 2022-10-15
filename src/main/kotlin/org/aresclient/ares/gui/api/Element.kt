package org.aresclient.ares.gui.api

import net.meshmc.mesh.api.render.Screen
import org.aresclient.ares.renderer.MatrixStack
import org.aresclient.ares.utils.Renderer
import org.aresclient.ares.utils.Theme
import java.util.*
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.math.max

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
        val tmp = ArrayList(getChildren())
        for(i in (tmp.size - 1) downTo 0) { // reverse because rendering flips order on screen
            val child = tmp[i]
            if(child.isVisible()) child.click(mouseX, mouseY, mouseButton, acted)
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
    private val screen = object: Screen(title) {
        private var first = true

        override fun init() {
            super.init()
            open = true

            matrixStack.projection().setOrtho(0F, width.toFloat(), height.toFloat(), 0F, 0F, 1F)

            if(first) {
                this@ScreenElement.init()
                first = false
            }
            this@ScreenElement.update()
        }

        override fun close() {
            super.close()
            this@ScreenElement.close()
            open = false
        }

        override fun render(mouseX: Int, mouseY: Int, partialTicks: Float) {
            Renderer.render(matrixStack) { buffers ->
                this@ScreenElement.draw(Theme.current(), buffers, matrixStack, mouseX, mouseY, partialTicks) // we don't need to push matrices for screen drawing
            }
            super.render(mouseX, mouseY, partialTicks)
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

    open fun init() {
    }

    override fun isVisible(): Boolean = open

    override fun getX(): Float = 0f
    override fun getY(): Float = 0f

    override fun getWidth(): Float = screen.width.toFloat()
    override fun getHeight(): Float = screen.height.toFloat()

    fun getScreen(): Screen = screen
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

open class DynamicElementGroup(private val columns: Int, private val columnWidth: () -> Float, private val childHeight: Float,
                               x: Float = 0f, y: Float = 0f): StaticElement(x, y, 0f, 0f) {
    init {
        if(columns < 1) throw RuntimeException("Fewer than 1 columns in GuiElementGroup is not possible")
    }

    override fun pushChild(child: Element): Element {
        if(child !is DynamicElement) throw RuntimeException("Cannot add static element to dynamic group!")

        val curr = getChildren().size
        val column = curr % columns

        child.setX { column * columnWidth.invoke() }
        child.setWidth(columnWidth)
        child.setHeight { childHeight }
        child.setVisible { isVisible() }

        if(curr >= columns) {
            val prev = getChildren()[max(column, curr - columns)]
            child.setY { prev.getY() + prev.getHeight() }
        } else child.setY { 0f }

        return super.pushChild(child)
    }

    override fun getHeight(): Float {
        return if(getChildren().size > 0) getChildren().peek().let { it.getY() + it.getHeight() } else 0f
    }
}
