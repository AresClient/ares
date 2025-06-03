package org.aresclient.ares.impl.instrument.modules.hud

import net.minecraft.client.util.math.MatrixStack
import net.minecraft.text.MutableText
import net.minecraft.text.PlainTextContent
import net.minecraft.text.Text
import org.aresclient.ares.api.nrender.font.TextColor
import org.aresclient.ares.api.nrender.drawer.HudDrawer
import org.aresclient.ares.api.util.Color
import org.aresclient.ares.impl.AresPlugin
import org.joml.Vector2d

object ToggleList: HudModule("Toggle List", "Shows whether specific modules are enabled or disabled.", position = Vector2d(0.5, 0.8)) {
    enum class Sort { SHORT_TO_LONG, LONG_TO_SHORT, ALPHABETICAL }

    private val delimiter = settings.addString("Delimiter", " : ")
    private val sort = settings.addEnum("Sort", Sort.SHORT_TO_LONG)

    private var shownModules = arrayListOf<ModuleInfo>()
    private var longestOffset = 0f
    private var delimiterOffset = 0f

    override fun onTick() {
        shownModules.clear()
        longestOffset = 0f

        for(module in AresPlugin.modules) {
            if(!module.externalCommons.showOnToggleList) continue

            val text = Text.literal(module.name)
            val width = getStringWidth(text)
            val info = ModuleInfo(text, width, module.isEnabled())
            shownModules.add(info)
            if(width > longestOffset) longestOffset = width
        }

        delimiterOffset = getStringWidth(Text.literal(delimiter.value)) / 2
        longestOffset += delimiterOffset

        if(sort.value == Sort.SHORT_TO_LONG || sort.value == Sort.LONG_TO_SHORT) shownModules.sortWith(compareBy { it.nameWidth })
        if(sort.value == Sort.LONG_TO_SHORT) shownModules.reverse()
        if(sort.value == Sort.ALPHABETICAL) shownModules.sortWith(compareBy { it.name.literalString })
    }

    override fun onRenderHud(drawer: HudDrawer, matrixStack: MatrixStack, delta: Float) {
        if(shownModules.isEmpty()) return

        for((i, info) in shownModules.withIndex()) {
            val xOffset = longestOffset - info.nameWidth
            drawer.drawText(getFont(), info.text, matrixStack, getX() + 1f + xOffset, getY() + 1f + i * getLineHeight(), Color.WHITE, size = getSize())
        }
    }

    private fun getLineHeight() = getFont().getHeight(getSize()) + 2f

    private fun getStringWidth(text: Text) = getFont().getWidth(text, getSize())

    override fun getWidth() = longestOffset * 2f + 2f

    override fun getHeight() = getLineHeight() * shownModules.size + 2f

    private data class ModuleInfo(val name: Text, val nameWidth: Float, val enabled: Boolean) {
        val text: Text = MutableText.of(PlainTextContent.EMPTY).also {
            it.append(name)
            it.append(delimiter.value)
            if(enabled) it.append("${TextColor.GREEN}Enabled")
            else it.append("${TextColor.RED}Disabled")
        }
    }
}
