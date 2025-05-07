package org.aresclient.ares.api.gui

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
