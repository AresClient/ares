package org.aresclient.ares.gui.api

import org.aresclient.ares.Ares
import org.aresclient.ares.gui.ClickGUI
import org.aresclient.ares.renderer.Buffer
import org.aresclient.ares.renderer.MatrixStack
import org.aresclient.ares.renderer.Shader
import org.aresclient.ares.renderer.VertexFormat
import org.aresclient.ares.utils.Renderer

// TODO: USE DYNAMIC BUFFERS INSTEAD OF CREATING A NEW STATIC BUFFER EACH DRAW CALL
class Window(
    var name: String,
    workingArea: DynamicElement,
    isWindowOpen: () -> Boolean = { false },
    spawnX: () -> Float = { 20f },
    spawnY: () -> Float = { 20f },
    width: () -> Float = { 360f },
    height: () -> Float = { 270f }
): DynamicElement(isWindowOpen, spawnX, spawnY, width, height) {
    private companion object {
        private val FONT_RENDERER = Renderer.getFontRenderer(14f)
        private const val TITLEBAR_HEIGHT = 30f
    }

    private val windowRadius = Shader.ROUNDED.uniformF1("radius").set(0.08f)
    private val windowSize = Shader.ROUNDED.uniformF2("size").set(getWidth(), getHeight())
    private val buffer = Buffer
        .createStatic(Shader.ROUNDED, VertexFormat.POSITION_UV_COLOR, 8, 12)
        .vertices(
            getWidth(), getHeight(), 0f, 1f, 1f, Ares.RED.red, Ares.RED.green, Ares.RED.blue, Ares.RED.alpha,
            getWidth(), 0f, 0f, 1f, -1f, Ares.RED.red, Ares.RED.green, Ares.RED.blue, Ares.RED.alpha,
            0f, getHeight(), 0f, -1f, 1f, Ares.RED.red, Ares.RED.green, Ares.RED.blue, Ares.RED.alpha,
            0f, 0f, 0f, -1f, -1f, Ares.RED.red, Ares.RED.green, Ares.RED.blue, Ares.RED.alpha,

            getWidth() - 2, getHeight() - 2, 0f, 1f, 1f, Ares.GRAY.red, Ares.GRAY.green, Ares.GRAY.blue, Ares.GRAY.alpha,
            getWidth() - 2, 2f, 0f, 1f, -1f, Ares.GRAY.red, Ares.GRAY.green, Ares.GRAY.blue, Ares.GRAY.alpha,
            2f, getHeight() - 2, 0f, -1f, 1f, Ares.GRAY.red, Ares.GRAY.green, Ares.GRAY.blue, Ares.GRAY.alpha,
            2f, 2f, 0f, -1f, -1f, Ares.GRAY.red, Ares.GRAY.green, Ares.GRAY.blue, Ares.GRAY.alpha,
        )
        .indices(
            0, 1, 2,
            1, 2, 3,
            4, 5, 6,
            5, 6, 7
        )
        .uniform(windowRadius)
        .uniform(windowSize)

    var dragging = false
    var lastClickX = -1
    var lastClickY = -1

    init {
        workingArea.setX { ClickGUI.padding.value + 2f }
            .setY { TITLEBAR_HEIGHT }
            .setWidth { getWidth() - 2 * (ClickGUI.padding.value + 2) }
            .setHeight { getHeight() - TITLEBAR_HEIGHT.toInt() - ClickGUI.padding.value - 2 }
        pushChild(workingArea)
    }

    override fun draw(matrixStack: MatrixStack, mouseX: Int, mouseY: Int, delta: Float) {
        if(dragging) {
            if(lastClickX == -1 || lastClickY == -1) {
                lastClickX = mouseX
                lastClickY = mouseY
            }

            val newX = getX() + mouseX - lastClickX
            val newY = getY() + mouseY - lastClickY

            setX { newX }
            setY { newY }

            lastClickX = mouseX
            lastClickY = mouseY
        }

        buffer.draw(matrixStack)
        val textX = getWidth() / 2 - FONT_RENDERER.getStringWidth(name) / 2
        val textY = TITLEBAR_HEIGHT / 2 - FONT_RENDERER.charHeight / 4
        FONT_RENDERER.drawString(matrixStack, name, textX, textY, 1f, 1f, 1f)
    }

    override fun click(mouseX: Int, mouseY: Int, mouseButton: Int) {
        if(mouseButton == 0) {
            if(getParent()!!.getChildren().peek() != this) {
                getParent()!!.getChildren().remove(this)
                getParent()!!.getChildren().push(this)
            }

            getChildren().forEach { if(it.isMouseOver(mouseX.toFloat(), mouseY.toFloat())) return }

            dragging = true
        }
        super.click(mouseX, mouseY, mouseButton)
    }

    override fun release(mouseX: Int, mouseY: Int, mouseButton: Int) {
        if(mouseButton == 0) {
            dragging = false
            lastClickX = -1
            lastClickY = -1
        }
        super.release(mouseX, mouseY, mouseButton)
    }

    override fun delete() {
        buffer.delete()
        super.delete()
    }
}

class WorkingArea: DynamicElement() {
    private val areaRadius = Shader.ROUNDED.uniformF1("radius").set(0.08f)
    private val areaSize = Shader.ROUNDED.uniformF2("size").set(getWidth(), getHeight())
    private val buffer = Buffer
        .createStatic(Shader.ROUNDED, VertexFormat.POSITION_UV_COLOR, 8, 12)
        .vertices(
            getWidth().toFloat(),   getHeight().toFloat(),  0f, 1f, 1f,     Ares.RED.red, Ares.RED.green, Ares.RED.blue, Ares.RED.alpha,
            getWidth().toFloat(),   0f,                     0f, 1f, -1f,    Ares.RED.red, Ares.RED.green, Ares.RED.blue, Ares.RED.alpha,
            0f,                     getHeight().toFloat(),  0f, -1f, 1f,    Ares.RED.red, Ares.RED.green, Ares.RED.blue, Ares.RED.alpha,
            0f,                     0f,                     0f, -1f, -1f,   Ares.RED.red, Ares.RED.green, Ares.RED.blue, Ares.RED.alpha,

            getWidth().toFloat() - 1,   getHeight().toFloat() - 1,  0f, 1f, 1f,     Ares.GRAY.red, Ares.GRAY.green, Ares.GRAY.blue, Ares.GRAY.alpha,
            getWidth().toFloat() - 1,   1f,                         0f, 1f, -1f,    Ares.GRAY.red, Ares.GRAY.green, Ares.GRAY.blue, Ares.GRAY.alpha,
            1f,                         getHeight().toFloat() - 1,  0f, -1f, 1f,    Ares.GRAY.red, Ares.GRAY.green, Ares.GRAY.blue, Ares.GRAY.alpha,
            1f,                         1f,                         0f, -1f, -1f,   Ares.GRAY.red, Ares.GRAY.green, Ares.GRAY.blue, Ares.GRAY.alpha,
        )
        .indices(
            0, 1, 2,
            1, 2, 3,
            4, 5, 6,
            5, 6, 7
        )
        .uniform(areaRadius)
        .uniform(areaSize)

    override fun draw(matrixStack: MatrixStack, mouseX: Int, mouseY: Int, delta: Float) {
        buffer.draw(matrixStack)
    }

    override fun delete() {
        buffer.delete()
        super.delete()
    }
}