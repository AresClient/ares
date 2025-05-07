package org.aresclient.ares.api.gui

import org.aresclient.ares.api.Wrapper
import org.aresclient.ares.api.render.MatrixStack
import org.aresclient.ares.api.render.Renderer
import org.aresclient.ares.impl.util.Theme
import java.util.*
import java.util.concurrent.atomic.AtomicBoolean

abstract class Element: Wrapper {
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

    open fun pushChild(child:Element):Element {
        getChildren().push(child).setParent(this)
        child.update()
        return this
    }

    fun pushChildren(vararg children:Element):Element {
        children.forEach { pushChild(it) }
        return this
    }
    fun pushChildren(children: Iterable<Element>):Element {
        children.forEach { pushChild(it) }
        return this
    }
    fun removeChild(element:Element):Element {
        getChildren().remove(element)
        return this
    }
    open fun popChild():Element = getChildren().pop().setParent(null)

    fun getParent(): Element? = parent
    fun setParent(parent: Element?):Element {
        this.parent = parent
        return this
    }

    fun getRootParent(): Element? {
        var element = this.getParent()
        while(true) element = element?.getParent() ?: return element
    }

    fun isMouseOver(mouseX: Int, mouseY: Int): Boolean = isMouseOver(mouseX.toDouble(), mouseY.toDouble())
    fun isMouseOver(mouseX: Float, mouseY: Float): Boolean = isMouseOver(mouseX.toDouble(), mouseY.toDouble())
    open fun isMouseOver(mouseX: Double, mouseY: Double): Boolean =
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
    open fun click(mouseX: Double, mouseY: Double, mouseButton: Int, acted: AtomicBoolean) {
        getChildren().reversed().forEach {
            if(it.isVisible()) it.click(mouseX, mouseY, mouseButton, acted)
        }
    }

    open fun release(mouseX: Double, mouseY: Double, mouseButton: Int) {
        ArrayList(getChildren()).forEach {
            if(it.isVisible()) it.release(mouseX, mouseY, mouseButton)
        }
    }

    open fun type(typedChar: Char?, keyCode: Int) {
        getChildren().forEach {
            if(it.isVisible()) it.type(typedChar, keyCode)
        }
    }

    open fun scroll(mouseX: Double, mouseY: Double, value: Double, acted: AtomicBoolean) {
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
