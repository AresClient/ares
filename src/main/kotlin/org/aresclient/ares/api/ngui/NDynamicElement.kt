package org.aresclient.ares.api.ngui

// an element with constantly changing dimensions or position
open class NDynamicElement(
    private var visible: () -> Boolean = { true },
    private var x: () -> Float = { 0f },
    private var y: () -> Float = { 0f },
    private var width: () -> Float = { 0f },
    private var height: () -> Float = { 0f }
): NElement() {
    override fun isVisible(): Boolean = visible.invoke()

    override fun getX(): Float = x.invoke()

    override fun getY(): Float = y.invoke()

    override fun getWidth(): Float = width.invoke()

    override fun getHeight(): Float = height.invoke()

    fun setVisible(value: () -> Boolean): NDynamicElement {
        visible = value
        return this
    }

    fun setX(value: () -> Float): NDynamicElement {
        x = value
        return this
    }

    fun setY(value: () -> Float): NDynamicElement {
        y = value
        return this
    }

    fun setWidth(value: () -> Float): NDynamicElement {
        width = value
        return this
    }

    fun setHeight(value: () -> Float): NDynamicElement {
        height = value
        return this
    }
 }
