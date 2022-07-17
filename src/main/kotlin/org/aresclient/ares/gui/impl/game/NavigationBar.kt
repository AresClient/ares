package org.aresclient.ares.gui.impl.game

import org.aresclient.ares.gui.api.Button
import org.aresclient.ares.gui.api.StaticElement
import org.aresclient.ares.renderer.MatrixStack
import org.aresclient.ares.utils.Renderer
import org.aresclient.ares.utils.Renderer.draw
import org.aresclient.ares.utils.Theme

class NavigationBar(private val top: Float, private val windows: List<Window>): StaticElement() {
    val padding = top / 6f

    init {
        windows.forEachIndexed { index, window ->
            pushChild(NavButton(this, index, window))
        }
    }

    override fun update() {
        setWidth(windows.size * (top - 12 + padding) + padding)
        setHeight(top)
        setX((getRootParent()!!.getWidth() / 2f) - (getWidth() / 2f))

        super.update()
    }

    override fun draw(theme: Theme, buffers: Renderer.Buffers, matrixStack: MatrixStack, mouseX: Int, mouseY: Int, delta: Float) {
        buffers.uniforms.roundedRadius.set(0.1f)
        buffers.uniforms.roundedSize.set(getWidth(), getHeight() * 2)
        buffers.rounded.draw(matrixStack) {
            vertices(
                0f, 0f, 0f, -1f, 0f, theme.background.red, theme.background.green, theme.background.blue, theme.background.alpha,
                getWidth(), 0f, 0f, 1f, 0f, theme.background.red, theme.background.green, theme.background.blue, theme.background.alpha,
                0f, getHeight(), 0f, -1f, 1f, theme.background.red, theme.background.green, theme.background.blue, theme.background.alpha,
                getWidth(), getHeight(), 0f, 1f, 1f, theme.background.red, theme.background.green, theme.background.blue, theme.background.alpha
            )
            indices(
                0, 1, 2,
                1, 2, 3
            )
        }

        buffers.lines.draw(matrixStack) {
            vertices(
                0f, 0f, 0f, 1f, theme.primary.red, theme.primary.green, theme.primary.blue, theme.primary.alpha,
                getWidth(), 0f, 0f, 1f, theme.primary.red, theme.primary.green, theme.primary.blue, theme.primary.alpha,
            )
            indices(0, 1)
        }

        super.draw(theme, buffers, matrixStack, mouseX, mouseY, delta)
    }

    private class NavButton(private val navigationBar: NavigationBar, index: Int, private val window: Window):
        Button((navigationBar.top - 12 + navigationBar.padding) * index + navigationBar.padding, 4f,
        navigationBar.top - 12, navigationBar.top - 12, { window.setVisible(!window.isVisible()) }, clickAnimation = false) {
        override fun draw(theme: Theme, buffers: Renderer.Buffers, matrixStack: MatrixStack, mouseX: Int, mouseY: Int) {
            if(holding) matrixStack.model().translate(0f, 1f, 0f)

            window.getIcon().bind()

            if(hovering && navigationBar.windows.none { it.isMouseOver(mouseX, mouseY) }) {
                Renderer.clip({ draw(buffers, matrixStack) }) {
                    buffers.triangle.draw(matrixStack) {
                        vertices(
                            0f, 0f, 0f, theme.lightground.red, theme.lightground.green, theme.lightground.blue, 0.15f,
                            getWidth(), 0f, 0f, theme.lightground.red, theme.lightground.green, theme.lightground.blue, 0.15f,
                            0f, getHeight(), 0f, theme.lightground.red, theme.lightground.green, theme.lightground.blue, 0.15f,
                            getWidth(), getHeight(), 0f, theme.lightground.red, theme.lightground.green, theme.lightground.blue, 0.15f
                        )
                        indices(
                            0, 1, 2,
                            1, 2, 3
                        )
                    }
                }
            } else draw(buffers, matrixStack)

            if(window.isVisible()) {
                val size = navigationBar.top / 28f

                matrixStack.model().translate(getWidth() / 2f, getHeight() + size + 3, 0f)
                if(holding) matrixStack.model().translate(0f, -1f, 0f)

                buffers.ellipse.draw(matrixStack) {
                    vertices(
                        size, size, 0f, 1f, 1f, theme.primary.red, theme.primary.green, theme.primary.blue, theme.primary.alpha,
                        size, -size, 0f, 1f, -1f, theme.primary.red, theme.primary.green, theme.primary.blue, theme.primary.alpha,
                        -size, size, 0f, -1f, 1f, theme.primary.red, theme.primary.green, theme.primary.blue, theme.primary.alpha,
                        -size, -size, 0f, -1f, -1f, theme.primary.red, theme.primary.green, theme.primary.blue, theme.primary.alpha
                    )
                    indices(
                        0, 1, 2,
                        1, 2, 3
                    )
                }
            }
        }

        private fun draw(buffers: Renderer.Buffers, matrixStack: MatrixStack) {
            buffers.triangleTex.draw(matrixStack) {
                vertices(
                    0f, 0f, 0f, 0f, 0f,
                    0f, getHeight(), 0f, 0f, 1f,
                    getWidth(), getHeight(), 0f, 1f, 1f,
                    getWidth(), 0f, 0f, 1f, 0f
                )
                indices(
                    0, 1, 2,
                    2, 0, 3
                )
            }
        }
    }
}
