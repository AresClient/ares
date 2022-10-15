package org.aresclient.ares.gui.impl.game.setting

import org.aresclient.ares.SColor
import org.aresclient.ares.Serializable
import org.aresclient.ares.Setting
import org.aresclient.ares.Settings
import org.aresclient.ares.gui.api.Element
import org.aresclient.ares.gui.impl.game.SettingElement
import org.aresclient.ares.gui.impl.game.SettingsContent
import org.aresclient.ares.gui.impl.game.SettingsContent.Companion.open
import org.aresclient.ares.gui.impl.game.SettingsGroup
import org.aresclient.ares.renderer.MatrixStack
import org.aresclient.ares.utils.Renderer
import org.aresclient.ares.utils.Renderer.draw
import org.aresclient.ares.utils.Theme
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.math.pow
import kotlin.math.sqrt

private const val PADDING = 2f

class CategoryElement(private val serializable: Serializable, private val content: SettingsContent, defaultHeight: Float):
    SettingElement(defaultHeight, start = if(serializable is Settings) defaultHeight else 3f) {
    private val windowButton = WindowButton(this)
    private val openable = serializable is Settings
    private var open = false
    private val enabled: Setting<Boolean>? = (serializable as? Settings)?.getMap()?.get("Enabled") as? Setting<Boolean>
    private val group = if(openable) SettingsGroup(serializable, content, 1, { getWidth() - PADDING },
        defaultHeight * 0.87f, x = PADDING, y = defaultHeight, skipEnabled = enabled != null).also { it.setVisible(open) } else null

    init {
        pushChild(SettingElementButton(this) {
            if(enabled != null) enabled.value = !enabled.value
            else content.open(serializable)
        })
        pushChild(windowButton)
        if(enabled != null) pushChild(ForwardButton(this, windowButton))
        group?.also { pushChild(it) }
    }

    override fun getText(): String = serializable.getName()
    override fun getTextColor(theme: Theme): SColor = if(enabled?.value == true) theme.primary else theme.lightground

    override fun click(mouseX: Int, mouseY: Int, mouseButton: Int, acted: AtomicBoolean) {
        if(openable && isMouseOver(mouseX, mouseY) && !acted.get() && mouseY <= getRenderY() + defaultHeight && (mouseButton == 1
                    || (mouseButton == 0 && mouseX <= getRenderX() + defaultHeight))) {
            open = !open
            group!!.setVisible(open)
            acted.set(true)
        }

        super.click(mouseX, mouseY, mouseButton, acted)
    }

    override fun getHeight(): Float {
        return if(open) group!!.getHeight() + defaultHeight
        else defaultHeight
    }

    override fun draw(theme: Theme, buffers: Renderer.Buffers, matrixStack: MatrixStack, mouseX: Int, mouseY: Int, delta: Float) {
        super.draw(theme, buffers, matrixStack, mouseX, mouseY, delta)

        if(openable) {
            buffers.lines.draw(matrixStack) {
                val padding = defaultHeight / 4f
                val half = defaultHeight / 2f
                if(open) {
                    vertices(
                        padding, half, 0f, 2f, theme.lightground.red, theme.lightground.green, theme.lightground.blue, theme.lightground.alpha,
                        defaultHeight - padding, half, 0f, 2f, theme.lightground.red, theme.lightground.green, theme.lightground.blue, theme.lightground.alpha,
                    )
                    indices(0, 1)
                } else {
                    vertices(
                        padding, half, 0f, 2f, theme.lightground.red, theme.lightground.green, theme.lightground.blue, theme.lightground.alpha,
                        defaultHeight - padding, half, 0f, 2f, theme.lightground.red, theme.lightground.green, theme.lightground.blue, theme.lightground.alpha,
                        half, padding, 0f, 2f, theme.lightground.red, theme.lightground.green, theme.lightground.blue, theme.lightground.alpha,
                        half, defaultHeight - padding, 0f, 2f, theme.lightground.red, theme.lightground.green, theme.lightground.blue, theme.lightground.alpha
                    )
                    indices(0, 1, 2, 3)
                }
            }
        }

        if(open) {
            buffers.triangle.draw(matrixStack) {
                vertices(
                    0f, defaultHeight, 0f, theme.primary.red, theme.primary.green, theme.primary.blue, theme.primary.alpha,
                    PADDING, defaultHeight, 0f, theme.primary.red, theme.primary.green, theme.primary.blue, theme.primary.alpha,
                    0f, getHeight(), 0f, theme.primary.red, theme.primary.green, theme.primary.blue, theme.primary.alpha,
                    PADDING, getHeight(), 0f, theme.primary.red, theme.primary.green, theme.primary.blue, theme.primary.alpha
                )
                indices(
                    0, 1, 2,
                    1, 2, 3
                )
            }

            buffers.lines.draw(matrixStack) {
                vertices(
                    0f, defaultHeight, 0f, 2f, theme.primary.red, theme.primary.green, theme.primary.blue, theme.primary.alpha,
                    getWidth(), defaultHeight, 0f, 2f, theme.primary.red, theme.primary.green, theme.primary.blue, theme.primary.alpha
                )
                indices(0, 1)
            }
        }
    }

    private class WindowButton(private val categoryElement: CategoryElement):
        SettingSubButton(categoryElement.defaultHeight, {
            categoryElement.content.getWindow()?.duplicate()?.open(categoryElement.serializable)
        }) {
        val size = getHeight()
        val mid = size / 2f
        val padding = size / 3.75f
        val paddingCorner = mid - sqrt(padding * padding / 2f)

        override fun getX(): Float = categoryElement.getWidth() - getY() - getWidth()

        override fun draw(theme: Theme, buffers: Renderer.Buffers, matrixStack: MatrixStack, mouseX: Int, mouseY: Int) {
            var color = theme.primary
            buffers.ellipse.draw(matrixStack) {
                vertices(
                    size, size, 0f, 1f, 1f, color.red, color.green, color.blue, color.alpha,
                    size, 0f, 0f, 1f, -1f, color.red, color.green, color.blue, color.alpha,
                    0f, size, 0f, -1f, 1f, color.red, color.green, color.blue, color.alpha,
                    0f, 0f, 0f, -1f, -1f, color.red, color.green, color.blue, color.alpha
                )
                indices(
                    0, 1, 2,
                    1, 2, 3
                )
            }

            color = theme.lightground
            buffers.lines.draw(matrixStack) {
                vertices(
                    paddingCorner, paddingCorner, 0f, 2f, color.red, color.green, color.blue, color.alpha,
                    size - paddingCorner, size - paddingCorner, 0f, 2f, color.red, color.green, color.blue, color.alpha,
                    paddingCorner, size - paddingCorner, 0f, 2f, color.red, color.green, color.blue, color.alpha,
                    size - paddingCorner, paddingCorner, 0f, 2f, color.red, color.green, color.blue, color.alpha
                )
                indices(
                    0, 2,   2, 1,
                    1, 3,   3, 0
                )
            }
        }

        override fun isMouseOver(mouseX: Float, mouseY: Float): Boolean {
            val halfW = getWidth() / 2f
            val halfH = getHeight() / 2f
            return (mouseX - getRenderX() - halfW).pow(2) / halfW.pow(2) + (mouseY - getRenderY() - halfH).pow(2) / halfH.pow(2) <= 1
        }
    }

    private class ForwardButton(private val categoryElement: CategoryElement, private val element: Element):
        SettingSubButton(categoryElement.defaultHeight, {
            categoryElement.content.open(categoryElement.serializable)
        }) {
        val size = getHeight()
        val mid = size / 2f
        val padding = size / 3.75f

        override fun getX(): Float = element.getX() - getY() - getWidth()

        override fun draw(theme: Theme, buffers: Renderer.Buffers, matrixStack: MatrixStack, mouseX: Int, mouseY: Int) {
            var color = theme.primary
            buffers.ellipse.draw(matrixStack) {
                vertices(
                    size, size, 0f, 1f, 1f, color.red, color.green, color.blue, color.alpha,
                    size, 0f, 0f, 1f, -1f, color.red, color.green, color.blue, color.alpha,
                    0f, size, 0f, -1f, 1f, color.red, color.green, color.blue, color.alpha,
                    0f, 0f, 0f, -1f, -1f, color.red, color.green, color.blue, color.alpha
                )
                indices(
                    0, 1, 2,
                    1, 2, 3
                )
            }

            color = theme.lightground
            buffers.lines.draw(matrixStack) {
                vertices(
                    padding, mid, 0f, 2f, color.red, color.green, color.blue, color.alpha,
                    mid, mid, 0f, 2f, color.red, color.green, color.blue, color.alpha
                )
                indices(0, 1)
            }

            buffers.triangle.draw(matrixStack) {
                vertices(
                    size - padding, mid, 0f, color.red, color.green, color.blue, color.alpha,
                    mid, padding, 0f, color.red, color.green, color.blue, color.alpha,
                    mid, size - padding, 0f, color.red, color.green, color.blue, color.alpha
                )
                indices(0, 1, 2)
            }
        }

        override fun isMouseOver(mouseX: Float, mouseY: Float): Boolean {
            val halfW = getWidth() / 2f
            val halfH = getHeight() / 2f
            return (mouseX - getRenderX() - halfW).pow(2) / halfW.pow(2) + (mouseY - getRenderY() - halfH).pow(2) / halfH.pow(2) <= 1
        }
    }
}
