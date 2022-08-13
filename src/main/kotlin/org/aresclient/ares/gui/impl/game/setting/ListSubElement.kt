package org.aresclient.ares.gui.impl.game.setting

import org.aresclient.ares.ListValues
import org.aresclient.ares.Setting
import org.aresclient.ares.gui.impl.game.CircleSettingSubButton
import org.aresclient.ares.gui.impl.game.SettingElement
import org.aresclient.ares.gui.impl.game.SettingsContent
import org.aresclient.ares.renderer.MatrixStack
import org.aresclient.ares.utils.Renderer
import org.aresclient.ares.utils.Renderer.draw
import org.aresclient.ares.utils.Theme

class ListSubElement(name: String, added: Boolean, content: SettingsContent): SettingElement(name, {
    (it as ListSubElement).toggle.click()
}) {
    private val toggle = if(added) RemoveButton(name, content) else AddButton(name, content)

    init {
        pushChild(toggle)
    }

    @Suppress("UNCHECKED_CAST")
    private class AddButton(name: String, content: SettingsContent): CircleSettingSubButton({
        val serializable = content.getSerializable() as Setting<ArrayList<Any>>
        val first = (serializable.possibleValues as ListValues<*>).values[0]
        if(first is Enum<*>) first.javaClass.enumConstants.forEach { if(it.name == name) serializable.value.add(it) }
        else serializable.value.add(name)

        content.refresh()
    }) {
        override fun draw(theme: Theme, buffers: Renderer.Buffers, matrixStack: MatrixStack, mouseX: Int, mouseY: Int) {
            super.draw(theme, buffers, matrixStack, mouseX, mouseY)

            val size = getHeight()
            val padding = size / 3.75f
            val mid = size / 2f

            buffers.lines.draw(matrixStack) {
                vertices(
                    mid, padding, 0f, 2f, theme.lightground.red, theme.lightground.green, theme.lightground.blue, theme.lightground.alpha,
                    mid, size - padding, 0f, 2f, theme.lightground.red, theme.lightground.green, theme.lightground.blue, theme.lightground.alpha,
                    padding, mid, 0f, 2f, theme.lightground.red, theme.lightground.green, theme.lightground.blue, theme.lightground.alpha,
                    size - padding, mid, 0f, 2f, theme.lightground.red, theme.lightground.green, theme.lightground.blue, theme.lightground.alpha
                )
                indices(0, 1, 2, 3)
            }
        }
    }

    @Suppress("UNCHECKED_CAST")
    private class RemoveButton(name: String, content: SettingsContent): CircleSettingSubButton({
        val serializable = content.getSerializable() as Setting<ArrayList<Any>>
        val first = (serializable.possibleValues as ListValues<*>).values[0]
        if(first is Enum<*>) first.javaClass.enumConstants.forEach { if(it.name == name) serializable.value.remove(it) }
        else serializable.value.remove(name)

        content.refresh()
    }) {
        override fun draw(theme: Theme, buffers: Renderer.Buffers, matrixStack: MatrixStack, mouseX: Int, mouseY: Int) {
            super.draw(theme, buffers, matrixStack, mouseX, mouseY)

            val size = getHeight()
            val padding = size / 3.75f
            val mid = size / 2f

            buffers.lines.draw(matrixStack) {
                vertices(
                    padding, mid, 0f, 2f, theme.lightground.red, theme.lightground.green, theme.lightground.blue, theme.lightground.alpha,
                    size - padding, mid, 0f, 2f, theme.lightground.red, theme.lightground.green, theme.lightground.blue, theme.lightground.alpha
                )
                indices(0, 1)
            }
        }
    }
}
