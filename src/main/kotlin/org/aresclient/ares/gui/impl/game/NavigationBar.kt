package org.aresclient.ares.gui.impl.game

import org.aresclient.ares.Settings
import org.aresclient.ares.gui.api.Button
import org.aresclient.ares.gui.api.StaticElement
import org.aresclient.ares.module.Category
import org.aresclient.ares.renderer.MatrixStack
import org.aresclient.ares.utils.Renderer
import org.aresclient.ares.utils.Renderer.draw
import org.aresclient.ares.utils.Theme

class NavigationBar(private val context: WindowContext, private val top: Float): StaticElement() {
    val padding = top / 6f

    init {
        pushChild(NavButton(this, 0, null))
        Category.values().forEachIndexed { index, category ->
            pushChild(NavButton(this, index + 1, category))
        }
    }

    override fun update() {
        setWidth((Category.values().size + 1) * (top - 12 + padding) + padding)
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
                0f, 0f, 0f, 2f, theme.primary.red, theme.primary.green, theme.primary.blue, theme.primary.alpha,
                getWidth(), 0f, 0f, 2f, theme.primary.red, theme.primary.green, theme.primary.blue, theme.primary.alpha,
            )
            indices(0, 1)
        }

        super.draw(theme, buffers, matrixStack, mouseX, mouseY, delta)
    }

    private class NavButton(private val navigationBar: NavigationBar, index: Int, private val category: Category?):
        Button((navigationBar.top - 12 + navigationBar.padding) * index + navigationBar.padding, 4f,
        navigationBar.top - 12, navigationBar.top - 12, clickAnimation = false) {

        private var open = false

        init {
            setAction {
                if(!open) {
                    if(category == null) navigationBar.context.open(Window(navigationBar.context).also { it.open(SettingsContent(Settings.new())) })
                    else navigationBar.context.open(Window(navigationBar.context).also { window -> window.open(SettingsContent(
                        Settings.new().also { it.string("setting", ":Modules:${category.prettyName}") })) })
                }
            }
            category?.let {
                navigationBar.context.getListeners().add {
                    open = navigationBar.context.getWindows().any {
                        it.getCurrentContent()?.let { content ->
                            content is SettingsContent && content.getPath() == ":Modules:${category.prettyName}"
                        } ?: false
                    }
                }
            }
        }

        override fun draw(theme: Theme, buffers: Renderer.Buffers, matrixStack: MatrixStack, mouseX: Int, mouseY: Int) {
            if(holding) matrixStack.model().translate(0f, 1f, 0f)

            if(category != null) category.icon.bind()
            else Window.DEFAULT_ICON.bind()

            if(hovering && navigationBar.context.getWindows().none { it.isMouseOver(mouseX, mouseY) }) {
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

            if(open) {
                val size = navigationBar.top / 22f

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
