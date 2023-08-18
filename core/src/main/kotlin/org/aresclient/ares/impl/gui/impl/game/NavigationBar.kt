package org.aresclient.ares.impl.gui.impl.game

import org.aresclient.ares.api.module.Category
import org.aresclient.ares.impl.gui.api.Button
import org.aresclient.ares.impl.gui.api.StaticElement
import org.aresclient.ares.impl.util.RenderHelper
import org.aresclient.ares.impl.util.RenderHelper.draw
import org.aresclient.ares.impl.util.Theme
import org.aresclient.ares.api.render.MatrixStack
import org.aresclient.ares.api.render.Renderer
import org.aresclient.ares.api.render.Texture

val DEFAULT_ICON =
    Texture(NavigationBar::class.java.getResourceAsStream("/assets/ares/textures/icons/gears.png"))

class NavigationBar(private val windowManager: WindowManager, private val top: Float): StaticElement() {
    val padding = top / 6f

    init {
        pushChild(NavButton(this, 0, null))
        Category.getAll().forEachIndexed { index, category ->
            pushChild(NavButton(this, index + 1, category))
        }
    }

    override fun update() {
        setWidth((Category.getAll().size + 1) * (top - 12 + padding) + padding)
        setHeight(top)
        setX((getRootParent()!!.getWidth() / 2f) - (getWidth() / 2f))

        super.update()
    }

    override fun draw(theme: Theme, buffers: Renderer.Buffers, matrixStack: MatrixStack, mouseX: Int, mouseY: Int, delta: Float) {
        buffers.uniforms.roundedRadius.set(0.09f)
        buffers.uniforms.roundedSize.set(getWidth(), getHeight() * 2)
        buffers.rounded.draw(matrixStack) {
            vertices(
                0f, 0f, 0f, -1f, 0f, theme.background.value.red, theme.background.value.green, theme.background.value.blue, theme.background.value.alpha,
                getWidth(), 0f, 0f, 1f, 0f, theme.background.value.red, theme.background.value.green, theme.background.value.blue, theme.background.value.alpha,
                0f, getHeight(), 0f, -1f, 1f, theme.background.value.red, theme.background.value.green, theme.background.value.blue, theme.background.value.alpha,
                getWidth(), getHeight(), 0f, 1f, 1f, theme.background.value.red, theme.background.value.green, theme.background.value.blue, theme.background.value.alpha
            )
            indices(
                0, 1, 2,
                1, 2, 3
            )
        }

        buffers.lines.draw(matrixStack) {
            vertices(
                0f, 0f, 0f, 3f, theme.primary.value.red, theme.primary.value.green, theme.primary.value.blue, theme.primary.value.alpha,
                getWidth(), 0f, 0f, 3f, theme.primary.value.red, theme.primary.value.green, theme.primary.value.blue, theme.primary.value.alpha
            )
            indices(0, 1)
        }

        super.draw(theme, buffers, matrixStack, mouseX, mouseY, delta)
    }

    private class NavButton(private val navigationBar: NavigationBar, index: Int, private val category: Category?):
        Button((navigationBar.top - 12 + navigationBar.padding) * index + navigationBar.padding, 4f,
        navigationBar.top - 12, navigationBar.top - 12, clipping = Clipping.NONE) {

        private var open = false

        init {
            setAction {
                navigationBar.windowManager.open("test")
            }
            /*setAction {
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
            }*/
        }

        override fun draw(theme: Theme, buffers: Renderer.Buffers, matrixStack: MatrixStack, mouseX: Int, mouseY: Int) {
            if(holding) matrixStack.model().translate(0f, 1f, 0f)

            if(category != null) category.icon.bind()
            else DEFAULT_ICON.bind()

            if(hovering /*&& navigationBar.context.getWindows().none { it.isMouseOver(mouseX, mouseY) }*/) {
                RenderHelper.clip({ draw(buffers, matrixStack) }) {
                    buffers.triangle.draw(matrixStack) {
                        vertices(
                            0f, 0f, 0f, theme.lightground.value.red, theme.lightground.value.green, theme.lightground.value.blue, 0.15f,
                            getWidth(), 0f, 0f, theme.lightground.value.red, theme.lightground.value.green, theme.lightground.value.blue, 0.15f,
                            0f, getHeight(), 0f, theme.lightground.value.red, theme.lightground.value.green, theme.lightground.value.blue, 0.15f,
                            getWidth(), getHeight(), 0f, theme.lightground.value.red, theme.lightground.value.green, theme.lightground.value.blue, 0.15f
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
                        size, size, 0f, 1f, 1f, theme.primary.value.red, theme.primary.value.green, theme.primary.value.blue, theme.primary.value.alpha,
                        size, -size, 0f, 1f, -1f, theme.primary.value.red, theme.primary.value.green, theme.primary.value.blue, theme.primary.value.alpha,
                        -size, size, 0f, -1f, 1f, theme.primary.value.red, theme.primary.value.green, theme.primary.value.blue, theme.primary.value.alpha,
                        -size, -size, 0f, -1f, -1f, theme.primary.value.red, theme.primary.value.green, theme.primary.value.blue, theme.primary.value.alpha
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
