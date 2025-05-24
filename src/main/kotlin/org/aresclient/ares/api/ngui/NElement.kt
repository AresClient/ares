package org.aresclient.ares.api.ngui

import net.minecraft.client.util.math.MatrixStack
import org.aresclient.ares.api.Wrapper
import org.aresclient.ares.api.nrender.Drawer
import org.aresclient.ares.impl.util.Theme
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.collections.ArrayList

abstract class NElement: Wrapper {
    private var dirty = true
    private val children = ArrayList<NElement>()
    private var parent: NElement? = null

    abstract fun isVisible(): Boolean

    abstract fun getX(): Float

    abstract fun getY(): Float

    abstract fun getWidth(): Float

    abstract fun getHeight(): Float

    open fun getRenderX(): Float = getX() + (getParent()?.getRenderX() ?: 0f)

    open fun getRenderY(): Float = getY() + (getParent()?.getRenderY() ?: 0f)

    fun markDirty() {
        getParent()?.markDirty()
    }

    fun getChildren(): List<NElement> = children

    open fun pushChild(child: NElement): NElement {
        child.setParent(this)
        children.add(child)
        return this
    }

    fun pushChildren(vararg children: NElement): NElement {
        children.forEach { pushChild(it) }
        return this
    }

    fun pushChildren(children: Iterable<NElement>): NElement {
        children.forEach { pushChild(it) }
        return this
    }

    fun removeChild(element: NElement): NElement {
        element.setParent(null)
        children.remove(element)
        return this
    }

    fun getParent(): NElement? = parent

    fun setParent(parent: NElement?): NElement {
        this.parent = parent
        return this
    }

    fun getRootParent(): NElement? {
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

    open fun draw(theme: Theme, drawer: Drawer, matrixStack: MatrixStack, mouseX: Int, mouseY: Int, delta: Float) {
        if(!dirty) return
        getChildren().forEach {
            if(it.isVisible()) it.render(theme, drawer, matrixStack, mouseX, mouseY, delta)
        }
    }

    fun render(theme: Theme, drawer: Drawer, matrixStack: MatrixStack, mouseX: Int, mouseY: Int, delta: Float) {
        matrixStack.push()
        matrixStack.translate(getX(), getY(), 0f)
        draw(theme, drawer, matrixStack, mouseX, mouseY, delta)
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

    open fun resize(width: Int, height: Int) {
        getChildren().forEach {
            it.resize(width, height)
        }
    }

    open fun close() {
        getChildren().forEach(NElement::close)
    }
}
