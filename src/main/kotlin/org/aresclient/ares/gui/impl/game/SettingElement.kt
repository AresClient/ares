package org.aresclient.ares.gui.impl.game

import net.meshmc.mesh.util.render.Color
import org.aresclient.ares.*
import org.aresclient.ares.gui.api.BaseElementGroup
import org.aresclient.ares.gui.api.Button
import org.aresclient.ares.gui.api.Element
import org.aresclient.ares.module.Category
import org.aresclient.ares.module.Module
import org.aresclient.ares.renderer.MatrixStack
import org.aresclient.ares.utils.Renderer
import org.aresclient.ares.utils.Renderer.draw
import org.aresclient.ares.utils.Theme
import kotlin.math.pow
import kotlin.math.sqrt

private val FONT_RENDERER = Renderer.getFontRenderer(12f)
private const val HEIGHT = 16f

class SettingsContent(settings: Settings): WindowContent(settings) {
    // TODO: CLEAN UP ElementGroup
    private val group = BaseElementGroup(childWidth = this::getWidth, childHeight = { HEIGHT })
    private val setting = settings.string("setting", "")
    private val serializable = setting.let {
        var serializable: Serializable = Ares.SETTINGS
        val split = it.value.split(":")
        for(name in split) {
            if(serializable is Settings) serializable = serializable.getMap()[name] ?: continue
            else break
        }
        serializable
    }

    init {
        setTitle(serializable.getName())

        // set icon if category
        for((ind, cat) in Module.CATEGORIES.withIndex()) {
            if(cat == serializable) {
                setIcon(Category.values()[ind].icon)
                break
            }
        }

        pushChild(group)
        refresh()
    }

    fun getSerializable(): Serializable = serializable

    fun getPath(): String = setting.value

    fun refresh() {
        group.getChildren().clear()

        when(serializable) {
            is Settings -> serializable.getMap().values.forEach {
                if(it.getName().first() != '.') group.pushChild(makeSettingElement(it, this))
            }
            is Setting<*> -> if(serializable.type == Setting.Type.LIST) {
                (serializable.value as List<*>).forEach { add(it, true) }
                (serializable.possibleValues as ListValues<*>).values.filterNot((serializable.value as List<*>)::contains).forEach { add(it, false) }
            }
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun add(any: Any?, added: Boolean) {
        if(any is Enum<*>) group.pushChild(ListSubElement(any.name, added, this))
        else if(any is String) group.pushChild(ListSubElement(any, added, this))
    }

    fun forward(name: String): SettingsContent =
        SettingsContent(Settings.new().also { it.string("setting", "${setting.value}:$name") })

    override fun getContentHeight(): Float = group.getHeight()
}

private fun makeSettingElement(serializable: Serializable, content: SettingsContent): Element =
    if(serializable is Settings || (serializable is Setting<*> && serializable.type == Setting.Type.LIST))
        CategoryElement(serializable, content)
    else SettingElement(serializable.getName()) {} // TODO: OTHER SETTING ELEMENTS

private abstract class SettingSubButton(action: (Button) -> Unit): Button(0f, 0f, 0f, 0f, action) {
    override fun getX(): Float = getParent()?.getWidth()?.let { it - getY() - getWidth()  } ?: 0f

    override fun getY(): Float = getParent()?.getHeight()?.let { it * 0.15f } ?: 0f

    override fun getHeight(): Float = getParent()?.getHeight()?.let { it * 0.7f } ?: 0f
}

private open class CircleButton(action: (Button) -> Unit): SettingSubButton(action) {
    override fun getWidth(): Float = getHeight()

    override fun draw(theme: Theme, buffers: Renderer.Buffers, matrixStack: MatrixStack, mouseX: Int, mouseY: Int) {
        if(isMouseOver(mouseX, mouseY)) circle(buffers, matrixStack, theme.secondary)
        else circle(buffers, matrixStack, theme.primary)
    }

    private fun circle(buffers: Renderer.Buffers, matrixStack: MatrixStack, color: Color) {
        val size = getHeight()

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
    }

    override fun isMouseOver(mouseX: Float, mouseY: Float): Boolean {
        val halfW = getWidth() / 2f
        val halfH = getHeight() / 2f
        return (mouseX - getRenderX() - halfW).pow(2) / halfW.pow(2) + (mouseY - getRenderY() - halfH).pow(2) / halfH.pow(2) <= 1
    }
}

private open class SettingElement(private val text: String, action: (Button) -> Unit):
    Button(0f, 0f, 0f, 0f, action, Clipping.SCISSOR) {
    override fun draw(theme: Theme, buffers: Renderer.Buffers, matrixStack: MatrixStack, mouseX: Int, mouseY: Int) {
        buffers.lines.draw(matrixStack) {
            vertices(
                0f, getHeight(), 0f, 1f, theme.primary.red, theme.primary.green, theme.primary.blue, theme.primary.alpha,
                getWidth(), getHeight(), 0f, 1f, theme.primary.red, theme.primary.green, theme.primary.blue, theme.primary.alpha
            )
            indices(
                0, 1
            )
        }

        FONT_RENDERER.drawString(
            matrixStack, text,
            3f, 1f, 1f, 1f, 1f, 1f
        )
    }
}

private class CategoryElement(private val serializable: Serializable, content: SettingsContent):
    SettingElement(serializable.getName(), { content.open(content.forward(serializable.getName())) }) {
    private val windowButton = WindowButton(serializable, content)

    init {
        pushChild(windowButton)
    }

    private class WindowButton(serializable: Serializable, content: SettingsContent):
        CircleButton({ content.getWindow()?.duplicate()?.open(content.forward(serializable.getName())) }) {
        override fun draw(theme: Theme, buffers: Renderer.Buffers, matrixStack: MatrixStack, mouseX: Int, mouseY: Int) {
            super.draw(theme, buffers, matrixStack, mouseX, mouseY)

            if(isMouseOver(mouseX, mouseY)) square(buffers, matrixStack, theme.lightground)
            else square(buffers, matrixStack, theme.lightground)
        }

        private fun square(buffers: Renderer.Buffers, matrixStack: MatrixStack, color: Color) {
            val size = getHeight()
            val mid = size / 2f
            val padding = size / 3.75f
            val paddingCorner = mid - sqrt(padding * padding / 2f)

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
    }
}

private class ListSubElement(name: String, added: Boolean, content: SettingsContent): SettingElement(name, {
    (it as ListSubElement).toggle.click()
}) {
    private val toggle = if(added) RemoveButton(name, content) else AddButton(name, content)

    init {
        pushChild(toggle)
    }

    @Suppress("UNCHECKED_CAST")
    private class AddButton(name: String, content: SettingsContent): CircleButton({
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
    private class RemoveButton(name: String, content: SettingsContent): CircleButton({
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
