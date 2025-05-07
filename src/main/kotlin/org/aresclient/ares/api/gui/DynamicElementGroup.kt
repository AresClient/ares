package org.aresclient.ares.api.gui

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
            child.setY { prev.getY() + if(prev.isVisible()) prev.getHeight() else 0f }
        } else child.setY { 0f }

        return super.pushChild(child)
    }

    override fun getHeight(): Float {
        return if(getChildren().size > 0) getChildren().peek().let { it.getY() + it.getHeight() } else 0f
    }

    private fun getColumnWidth() = getWidth() / columns
}
