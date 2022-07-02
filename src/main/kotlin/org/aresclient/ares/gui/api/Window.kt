package org.aresclient.ares.gui.api

import org.aresclient.ares.Ares
import org.aresclient.ares.gui.ClickGUI
import org.aresclient.ares.renderer.Buffer
import org.aresclient.ares.renderer.MatrixStack
import org.aresclient.ares.renderer.Shader
import org.aresclient.ares.renderer.VertexFormat
import org.aresclient.ares.utils.Renderer
import java.awt.Font

// TODO: USE DYNAMIC BUFFERS INSTEAD OF CREATING A NEW STATIC BUFFER EACH DRAW CALL
class Window(
    var name: String,
    parent: Element?,
    matrixStack: MatrixStack,
    workingArea: GuiElement,
    isWindowOpen: () -> Boolean = { false },
    spawnX: () -> Int = { 20 },
    spawnY: () -> Int = { 20 },
    width: () -> Int = { 360 },
    height: () -> Int = { 270 }
): GuiElement(parent, matrixStack, isWindowOpen, spawnX, spawnY, width, height) {
    private companion object {
        private val FONT_RENDERER = Renderer.getFontRenderer(14f)
        private val TITLEBAR_HEIGHT = 30f
    }

    init {
        pushChild(workingArea)
            .setX { ClickGUI.padding.value + 2 }
            .setY { TITLEBAR_HEIGHT.toInt() }
            .setWidth { getWidth() - 2 * (ClickGUI.padding.value + 2) }
            .setHeight { getHeight() - TITLEBAR_HEIGHT.toInt() - ClickGUI.padding.value - 2 }
    }

    private val windowRadius = Shader.ROUNDED.uniformF1("radius").set(0.08f)
    private val windowSize = Shader.ROUNDED.uniformF2("size").set(getWidth().toFloat(), getHeight().toFloat())

    var dragging = false
    var lastClickX = -1
    var lastClickY = -1

    override fun onRender(mouseX: Int, mouseY: Int, partialTicks: Float) {
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

        Renderer.render2d {
            Buffer
                .createStatic(Shader.ROUNDED, VertexFormat.POSITION_UV_COLOR, 8, 12)
                .vertices(
                    getWidth().toFloat(),   getHeight().toFloat(),  0f, 1f, 1f,     Ares.RED.red, Ares.RED.green, Ares.RED.blue, Ares.RED.alpha,
                    getWidth().toFloat(),   0f,                     0f, 1f, -1f,    Ares.RED.red, Ares.RED.green, Ares.RED.blue, Ares.RED.alpha,
                    0f,                     getHeight().toFloat(),  0f, -1f, 1f,    Ares.RED.red, Ares.RED.green, Ares.RED.blue, Ares.RED.alpha,
                    0f,                     0f,                     0f, -1f, -1f,   Ares.RED.red, Ares.RED.green, Ares.RED.blue, Ares.RED.alpha,

                    getWidth().toFloat() - 2, getHeight().toFloat() - 2,  0f, 1f, 1f,     Ares.GRAY.red, Ares.GRAY.green, Ares.GRAY.blue, Ares.GRAY.alpha,
                    getWidth().toFloat() - 2, 2f,                         0f, 1f, -1f,    Ares.GRAY.red, Ares.GRAY.green, Ares.GRAY.blue, Ares.GRAY.alpha,
                    2f,                       getHeight().toFloat() - 2,  0f, -1f, 1f,    Ares.GRAY.red, Ares.GRAY.green, Ares.GRAY.blue, Ares.GRAY.alpha,
                    2f,                       2f,                         0f, -1f, -1f,   Ares.GRAY.red, Ares.GRAY.green, Ares.GRAY.blue, Ares.GRAY.alpha,
                )
                .indices(
                    0, 1, 2,
                    1, 2, 3,
                    4, 5, 6,
                    5, 6, 7
                )
                .uniform(windowRadius)
                .uniform(windowSize)
                .draw(getMatrixStack())

            val textX = getWidth() / 2 - FONT_RENDERER.getStringWidth(name) / 2
            val textY = TITLEBAR_HEIGHT / 2 - FONT_RENDERER.charHeight / 4
            FONT_RENDERER.drawString(getMatrixStack(), name, textX, textY, 1f, 1f, 1f)
        }
    }

    override fun onClick(mouseX: Int, mouseY: Int, mouseButton: Int) {
        if(mouseButton != 0) return

        if(getParent()!!.getChildren().peek() != this) {
            getParent()!!.getChildren().remove(this)
            getParent()!!.getChildren().push(this)
        }

        getChildren().forEach { if(it.isMouseOver(mouseX.toDouble(), mouseY.toDouble())) return }

        dragging = true
    }

    override fun onRelease(mouseX: Int, mouseY: Int, mouseButton: Int) {
        if(mouseButton != 0) return

        dragging = false
        lastClickX = -1
        lastClickY = -1
    }

    override fun onType(typedChar: Char?, keyCode: Int) {
    }

    override fun onScroll(mouseX: Double, mouseY: Double, value: Double) {
    }
}

class WorkingArea(matrixStack: MatrixStack): GuiElement(null, matrixStack) {
    override fun onRender(mouseX: Int, mouseY: Int, partialTicks: Float) {
        val areaRadius = Shader.ROUNDED.uniformF1("radius").set(0.08f)
        val areaSize = Shader.ROUNDED.uniformF2("size").set(getWidth().toFloat(), getHeight().toFloat())

        Renderer.render2d {
            Buffer
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
                .draw(getMatrixStack())
        }
    }

    override fun onClick(mouseX: Int, mouseY: Int, mouseButton: Int) {
    }

    override fun onRelease(mouseX: Int, mouseY: Int, mouseButton: Int) {
    }

    override fun onType(typedChar: Char?, keyCode: Int) {
    }

    override fun onScroll(mouseX: Double, mouseY: Double, value: Double) {
    }
}