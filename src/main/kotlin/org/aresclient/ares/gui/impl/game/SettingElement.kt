package org.aresclient.ares.gui.impl.game

import net.meshmc.mesh.util.render.Color
import org.aresclient.ares.*
import org.aresclient.ares.gui.api.BaseElementGroup
import org.aresclient.ares.gui.api.Button
import org.aresclient.ares.gui.impl.game.setting.BooleanElement
import org.aresclient.ares.gui.impl.game.setting.CategoryElement
import org.aresclient.ares.gui.impl.game.setting.ListSubElement
import org.aresclient.ares.module.Category
import org.aresclient.ares.module.Module
import org.aresclient.ares.renderer.MatrixStack
import org.aresclient.ares.utils.Renderer
import org.aresclient.ares.utils.Renderer.draw
import org.aresclient.ares.utils.Theme
import kotlin.math.pow

private val FONT_RENDERER = Renderer.getFontRenderer(13f)
private const val HEIGHT = 18f

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

    @Suppress("UNCHECKED_CAST")
    fun refresh() {
        group.getChildren().clear()

        when(serializable) {
            is Settings -> serializable.getMap().values.forEach {
                if(it.getName().first() != '.') group.pushChild(when(it) {
                    is Settings -> CategoryElement(it, this)
                    is Setting<*> -> when(it.type) {
                        Setting.Type.LIST -> CategoryElement(it, this)
                        Setting.Type.BOOLEAN -> BooleanElement(it as Setting<Boolean>)
                        else -> SettingElement(it.getName()) {}
                    }
                    else -> SettingElement(it.getName()) {}
                })
            }
            is Setting<*> -> if(serializable.type == Setting.Type.LIST) {
                (serializable.value as List<*>).forEach { addListElements(it, true) }
                (serializable.possibleValues as ListValues<*>).values
                    .filterNot((serializable.value as List<*>)::contains).forEach { addListElements(it, false) }
            }
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun addListElements(any: Any?, added: Boolean) {
        if(any is Enum<*>) group.pushChild(ListSubElement(any.name, added, this))
        else if(any is String) group.pushChild(ListSubElement(any, added, this))
    }

    fun forward(name: String): SettingsContent =
        SettingsContent(Settings.new().also { it.string("setting", "${setting.value}:$name") })

    override fun getContentHeight(): Float = group.getHeight()
}

open class SettingElement(private val text: String, action: (Button) -> Unit):
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

abstract class SettingSubButton(action: (Button) -> Unit): Button(0f, 0f, 0f, 0f, action) {
    override fun getX(): Float = getParent()?.getWidth()?.let { it - getY() - getWidth()  } ?: 0f

    override fun getY(): Float = getParent()?.getHeight()?.let { it * 0.15f } ?: 0f

    override fun getHeight(): Float = getParent()?.getHeight()?.let { it * 0.7f } ?: 0f
}

open class CircleSettingSubButton(action: (Button) -> Unit): SettingSubButton(action) {
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
