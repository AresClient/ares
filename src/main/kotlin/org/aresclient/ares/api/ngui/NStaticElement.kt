package org.aresclient.ares.api.ngui

// an element that doesn't change dimension or size
open class NStaticElement(
    private var x: Float = 0f,
    private var y: Float = 0f,
    private var width: Float = 0f,
    private var height: Float = 0f
): NElement() {
    private var visible = true

    override fun isVisible(): Boolean = visible

    override fun getX(): Float = x

    override fun getY(): Float = y

    override fun getWidth(): Float = width

    override fun getHeight(): Float = height

    fun setVisible(value: Boolean): NStaticElement {
        visible = value
        return this
    }

    fun setX(value: Float): NStaticElement {
        x = value
        return this
    }

    fun setY(value: Float): NStaticElement {
        y = value
        return this
    }

    fun setWidth(value: Float): NStaticElement {
        width = value
        return this
    }

    fun setHeight(value: Float): NStaticElement {
        height = value
        return this
    }
}
