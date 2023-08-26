package org.aresclient.ares.impl.gui.impl.game.setting

import org.aresclient.ares.api.render.MatrixStack
import org.aresclient.ares.api.render.Renderer
import org.aresclient.ares.api.setting.Setting
import org.aresclient.ares.impl.gui.impl.game.SettingElement
import org.aresclient.ares.impl.gui.impl.game.SettingsContent
import org.aresclient.ares.impl.gui.impl.game.SettingsGroup
import org.aresclient.ares.impl.gui.impl.game.WindowContent
import org.aresclient.ares.impl.util.Theme
import org.aresclient.ares.impl.util.RenderHelper.draw
import java.util.concurrent.atomic.AtomicBoolean

private const val PADDING = 1f

class MapElement(private val content: WindowContent, private val setting: Setting.Map<*>, private val defaultHeight: Float): SettingElement(defaultHeight, defaultHeight) {
    private var open = false
    private val enabled: Setting<Boolean>? = setting.value["Enabled"] as? Setting.Boolean
    private val group = SettingsGroup(setting, 1, content,  enabled != null, defaultHeight * 0.87f,
        width = { getWidth() - PADDING }, x = { PADDING }, y = { defaultHeight }, visible = { open })

    init {
        pushChild(SettingElementButton(this) {
            if(enabled != null) enabled.value = !enabled.value
            else content.getWindow()?.open {
                addString("setting", setting.path)
                SettingsContent::class.java
            }
        })
        pushChild(group)
    }

    override fun getText(): String = setting.getName()
    override fun getTextColor(theme: Theme): Setting.Color = if(enabled?.value == true) theme.primary else theme.lightground

    override fun click(mouseX: Int, mouseY: Int, mouseButton: Int, acted: AtomicBoolean) {
        if(isMouseOver(mouseX, mouseY) && !acted.get() && mouseY <= getRenderY() + defaultHeight && (mouseButton == 1
                    || (mouseButton == 0 && mouseX <= getRenderX() + defaultHeight))) {
            open = !open
            acted.set(true)
        }

        super.click(mouseX, mouseY, mouseButton, acted)
    }

    override fun getHeight(): Float {
        return if(open) group.getHeight() + defaultHeight
        else defaultHeight
    }

    override fun draw(theme: Theme, buffers: Renderer.Buffers, matrixStack: MatrixStack, mouseX: Int, mouseY: Int, delta: Float) {
        super.draw(theme, buffers, matrixStack, mouseX, mouseY, delta)

        buffers.lines.draw(matrixStack) {
            val padding = defaultHeight / 4f
            val half = defaultHeight / 2f
            if(open) {
                vertices(
                    padding, half, 0f, 2f, theme.lightground.value.red, theme.lightground.value.green, theme.lightground.value.blue, theme.lightground.value.alpha,
                    defaultHeight - padding, half, 0f, 2f, theme.lightground.value.red, theme.lightground.value.green, theme.lightground.value.blue, theme.lightground.value.alpha,
                )
                indices(0, 1)
            } else {
                vertices(
                    padding, half, 0f, 2f, theme.lightground.value.red, theme.lightground.value.green, theme.lightground.value.blue, theme.lightground.value.alpha,
                    defaultHeight - padding, half, 0f, 2f, theme.lightground.value.red, theme.lightground.value.green, theme.lightground.value.blue, theme.lightground.value.alpha,
                    half, padding, 0f, 2f, theme.lightground.value.red, theme.lightground.value.green, theme.lightground.value.blue, theme.lightground.value.alpha,
                    half, defaultHeight - padding, 0f, 2f, theme.lightground.value.red, theme.lightground.value.green, theme.lightground.value.blue, theme.lightground.value.alpha
                )
                indices(0, 1, 2, 3)
            }
        }

        if(open) {
            buffers.triangle.draw(matrixStack) {
                vertices(
                    0f, defaultHeight, 0f, theme.primary.value.red, theme.primary.value.green, theme.primary.value.blue, theme.primary.value.alpha,
                    PADDING, defaultHeight, 0f, theme.primary.value.red, theme.primary.value.green, theme.primary.value.blue, theme.primary.value.alpha,
                    0f, getHeight(), 0f, theme.primary.value.red, theme.primary.value.green, theme.primary.value.blue, theme.primary.value.alpha,
                    PADDING, getHeight(), 0f, theme.primary.value.red, theme.primary.value.green, theme.primary.value.blue, theme.primary.value.alpha
                )
                indices(
                    0, 1, 2,
                    1, 2, 3
                )
            }

            buffers.lines.draw(matrixStack) {
                vertices(
                    0f, defaultHeight, 0f, 2f, theme.primary.value.red, theme.primary.value.green, theme.primary.value.blue, theme.primary.value.alpha,
                    getWidth(), defaultHeight, 0f, 2f, theme.primary.value.red, theme.primary.value.green, theme.primary.value.blue, theme.primary.value.alpha
                )
                indices(0, 1)
            }
        }
    }
}
