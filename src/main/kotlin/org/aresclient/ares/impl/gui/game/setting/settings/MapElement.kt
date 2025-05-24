package org.aresclient.ares.impl.gui.game.setting.settings

import org.aresclient.ares.api.render.MatrixStack
import org.aresclient.ares.api.render.Renderer
import org.aresclient.ares.api.setting.MapSetting
import org.aresclient.ares.api.setting.Setting
import org.aresclient.ares.api.setting.settings.BooleanSetting
import org.aresclient.ares.impl.gui.game.setting.DropdownSettingContainer
import org.aresclient.ares.impl.gui.game.setting.RowSettingGroup
import org.aresclient.ares.impl.gui.game.setting.SettingsWindowContent
import org.aresclient.ares.impl.util.RenderHelper.draw
import org.aresclient.ares.impl.util.Theme

class MapElement(private val content: SettingsWindowContent, setting: MapSetting, scale: Float):
	DropdownSettingContainer<MapSetting, Map<String, Setting<*>>>(setting, scale) {
    private val enabled: Setting<Boolean>? = setting.value["Enabled"] as? BooleanSetting

    init {
        pushChild(RowButton(this) {
            if(enabled != null) enabled.value = !enabled.value
            else content.getWindow()?.open {
                addString("setting", setting.path)
                SettingsWindowContent::class.java
            }
        })
        setDropdown(RowSettingGroup(setting, 1, content, scale * 0.87f))
    }

    override fun draw(theme: Theme, buffers: Renderer.Buffers, matrixStack: MatrixStack, mouseX: Int, mouseY: Int, delta: Float) {
        if(enabled?.value == true) {
            val color = theme.primary.value
            val offset = 1f
            buffers.triangle.draw(matrixStack) {
                vertices(
                    offset, offset, 0f, color.red, color.green, color.blue, color.alpha,
                    getWidth() - offset, offset, 0f, color.red, color.green, color.blue, color.alpha,
                    getWidth() - offset, scale - offset, 0f, color.red, color.green, color.blue, color.alpha,
                    offset, scale - offset, 0f, color.red, color.green, color.blue, color.alpha
                )
                indices(
                    0, 1, 2,
                    0, 3, 2
                )
            }
        }

        super.draw(theme, buffers, matrixStack, mouseX, mouseY, delta)
    }
}
