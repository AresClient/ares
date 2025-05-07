package org.aresclient.ares.api.gui

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
