package org.aresclient.ares.gui.api

import net.meshmc.mesh.api.render.Screen
import org.aresclient.ares.renderer.MatrixStack
import java.util.*
import kotlin.math.floor

interface Element {
    fun getParent(): Element?
    fun getChildren(): Stack<GuiElement>

    fun isVisible(): Boolean
    fun getMatrixStack(): MatrixStack

    fun getX(): Int
    fun getY(): Int

    fun getWidth(): Int
    fun getHeight(): Int

    fun getRootParent(): Element? {
        var p: Element? = getParent() ?: return this
        while(p!!.getParent() != null) p = p.getParent()
        return p
    }

    fun getOnScreenX(): Int =
        if(getParent() is GuiElement) getX() + getParent()!!.getOnScreenX()
        else getX()

    fun getOnScreenY(): Int =
        if(getParent() is GuiElement) getY() + getParent()!!.getOnScreenY()
        else getY()

    fun isMouseOver(mouseX: Double, mouseY: Double): Boolean =
        mouseX >= getOnScreenX()
                && mouseX <= getOnScreenX() + getWidth()
                && mouseY >= getOnScreenY()
                && mouseY <= getOnScreenY() + getHeight()

    fun pushChild(child: GuiElement): GuiElement {
        getChildren().push(child)
            .setParent(this)
        return child
    }

    fun popChild(): GuiElement {
        return getChildren().pop().setParent(null)
    }

    fun numberOfChildren(): Int {
        return getChildren().size
    }

    fun onRender(mouseX: Int, mouseY: Int, partialTicks: Float)
    fun render(mouseX: Int, mouseY: Int, partialTicks: Float) {
        getMatrixStack().push()
        getMatrixStack().model().translate(getX().toFloat(), getY().toFloat(), 0f)

        onRender(mouseX, mouseY, partialTicks)
        getChildren().forEach { if(it.isVisible()) it.render(mouseX, mouseY, partialTicks) }

        getMatrixStack().pop()
    }

    fun onClick(mouseX: Int, mouseY: Int, mouseButton: Int)
    fun click(mouseX: Int, mouseY: Int, mouseButton: Int) {
        onClick(mouseX, mouseY, mouseButton)
        ArrayList(getChildren()).forEach { if(it.isVisible() && it.isMouseOver(mouseX.toDouble(), mouseY.toDouble())) it.click(mouseX, mouseY, mouseButton) }
    }

    fun onRelease(mouseX: Int, mouseY: Int, mouseButton: Int)
    fun release(mouseX: Int, mouseY: Int, mouseButton: Int) {
        onRelease(mouseX, mouseY, mouseButton)
        getChildren().forEach { if(it.isVisible() && it.isMouseOver(mouseX.toDouble(), mouseY.toDouble())) it.release(mouseX, mouseY, mouseButton) }
    }

    fun onType(typedChar: Char?, keyCode: Int)
    fun type(typedChar: Char?, keyCode: Int) {
        onType(typedChar, keyCode)
        getChildren().forEach { if(it.isVisible()) it.type(typedChar, keyCode) }
    }

    fun onScroll(mouseX: Double, mouseY: Double, value: Double)
    fun scroll(mouseX: Double, mouseY: Double, value: Double) {
        onScroll(mouseX, mouseY, value)
        getChildren().forEach { if(it.isVisible()) it.scroll(mouseX, mouseY, value) }
    }
}

abstract class ScreenElement(title: String) :Screen(title), Element {
    private val children = Stack<GuiElement>()
    private var open: Boolean = false

    override fun init() {
        super.init()
        open = true
    }

    override fun close() {
        super.close()
        open = false
    }

    override fun getParent(): Element? = null
    override fun getChildren(): Stack<GuiElement> = children

    override fun isVisible(): Boolean = open
    override fun getMatrixStack(): MatrixStack = MatrixStack()

    override fun getX(): Int = 0
    override fun getY(): Int = 0

    override fun render(mouseX: Int, mouseY: Int, partialTicks: Float) {
        super<Element>.render(mouseX, mouseY, partialTicks)
    }

    override fun click(mouseX: Int, mouseY: Int, mouseButton: Int) {
        super<Element>.click(mouseX, mouseY, mouseButton)
    }

    override fun release(mouseX: Int, mouseY: Int, mouseButton: Int) {
        super<Element>.release(mouseX, mouseY, mouseButton)
    }

    override fun type(typedChar: Char?, keyCode: Int) {
        super<Element>.type(typedChar, keyCode)
    }

    override fun scroll(mouseX: Double, mouseY: Double, value: Double) {
        super<Element>.scroll(mouseX, mouseY, value)
    }
}

abstract class GuiElement(
    private var parent: Element?,
    private var matrixStack: MatrixStack,
    private var visible: () -> Boolean = { true },
    private var x: () -> Int = { 0 },
    private var y: () -> Int = { 0 },
    private var width: () -> Int = { 0 },
    private var height: () -> Int = { 0 }
): Element {
    private val children = Stack<GuiElement>()

    override fun getParent(): Element? = parent
    override fun isVisible(): Boolean = visible.invoke()
    override fun getMatrixStack(): MatrixStack = matrixStack
    override fun getX(): Int = x.invoke()
    override fun getY(): Int = y.invoke()
    override fun getWidth(): Int = width.invoke()
    override fun getHeight(): Int = height.invoke()
    override fun getChildren(): Stack<GuiElement> = children

    fun setParent(parent: Element?): GuiElement {
        this.parent = parent
        return this
    }

    fun setVisible(value: () -> Boolean): GuiElement {
        visible = value
        return this
    }

    fun setMatrixStack(matrixStack: MatrixStack): GuiElement {
        this.matrixStack = matrixStack
        return this
    }

    fun setX(value: () -> Int): GuiElement {
        x = value
        return this
    }

    fun setY(value: () -> Int): GuiElement {
        y = value
        return this
    }

    fun setWidth(value: () -> Int): GuiElement {
        width = value
        return this
    }

    fun setHeight(value: () -> Int): GuiElement {
        height = value
        return this
    }
 }

abstract class GuiElementGroup(
    parent: Element?,
    matrixStack: MatrixStack,
    visible: () -> Boolean = { false },
    x: () -> Int = { 0 },
    y: () -> Int = { 0 },
    width: () -> Int = { 0 },
    height: () -> Int = { 0 },

    private var columns: () -> Int = { 1 },
    private var childWidth: () -> Int = { 0 },
    private var childHeight: () -> Int = { 0 },
    private var padding: () -> Int = { 0 },
    private var edgePadding: () -> Int = { 0 }
): GuiElement(parent, matrixStack, visible, x, y, width, height) {
    init {
        if(getColumns() < 1) throw RuntimeException("Fewer than 1 columns in GuiElementGroup is not possible")
    }

    fun getColumns(): Int = columns.invoke()
    fun getChildWidth(): Int = childWidth.invoke()
    fun getChildHeight(): Int = childHeight.invoke()
    fun getPadding(): Int = padding.invoke()
    fun getEdgePadding(): Int = edgePadding.invoke()

    fun setColumns(value: () -> Int): GuiElementGroup {
        this.columns = value
        return this
    }

    fun setChildWidth(value: () -> Int): GuiElementGroup {
        this.childWidth = value
        return this
    }

    fun setChildHeight(value: () -> Int): GuiElementGroup {
        this.childHeight = value
        return this
    }

    fun setPadding(value: () -> Int): GuiElementGroup {
        this.padding = value
        return this
    }

    fun setEdgePadding(value: () -> Int): GuiElementGroup {
        this.edgePadding = value
        return this
    }

    override fun pushChild(child: GuiElement): GuiElement {
        getChildren().push(child).setParent(this).setMatrixStack(getMatrixStack())
        getChildren().forEach(this::adjustChildProperties)
        return child
    }

    fun addChildren(vararg children: GuiElement): GuiElementGroup {
        children.forEach { getChildren().push(it).setParent(this).setMatrixStack(getMatrixStack()) }
        getChildren().forEach(this::adjustChildProperties)
        return this
    }

    private fun adjustChildProperties(child: GuiElement) {
        if(!getChildren().contains(child)) return

        child
            .setWidth(childWidth::invoke).setHeight(childHeight::invoke)
            .setX {
                var i = getChildren().indexOf(child)
                while(i + 1 > columns.invoke()) i -= columns.invoke()
                i * childWidth.invoke() + i * padding.invoke() + edgePadding.invoke()
            }
            .setY {
                val n = floor(getChildren().indexOf(child).toDouble() / columns.invoke()).toInt()
                n * childHeight.invoke() + n * padding.invoke() + edgePadding.invoke()
            }
    }
}