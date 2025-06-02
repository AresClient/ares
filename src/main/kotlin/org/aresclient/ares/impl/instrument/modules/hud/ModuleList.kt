package org.aresclient.ares.impl.instrument.modules.hud

import net.minecraft.client.util.math.MatrixStack
import net.minecraft.text.Text
import org.aresclient.ares.api.instruments.Module
import org.aresclient.ares.api.nrender.TextColor
import org.aresclient.ares.api.nrender.hud.HudDrawer
import org.aresclient.ares.api.util.Color
import org.aresclient.ares.impl.AresPlugin
import org.joml.Vector2d

object ModuleList: HudModule("Module List", "Shows modules that are currently enabled in a list.", position = Vector2d(1.0, 0.0), defaults = Defaults().setEnabled(true)) {
    enum class Sort { SHORT_TO_LONG, LONG_TO_SHORT, ALPHABETICAL }

    enum class Alignment { LEFT, RIGHT }

    private val sort = settings.addEnum("Sort", Sort.LONG_TO_SHORT)
    private val align = settings.addEnum("Align", Alignment.RIGHT)

    private var shownModules = mutableListOf<ModuleInfo>()

    private var longestWidth = 0f

    override fun onTick() {
        shownModules.clear()
        longestWidth = 0f

        for(module in AresPlugin.modules) {
            if(!module.externalCommons.showOnModuleList) continue

            val text = module.getModuleListText()
            val width = getStringWidth(text)
            shownModules.add(ModuleInfo(text, width))
            if(width > longestWidth) longestWidth = width
        }


        if(sort.value == Sort.SHORT_TO_LONG || sort.value == Sort.LONG_TO_SHORT) shownModules.sortWith(compareBy { it.width })
        if(sort.value == Sort.LONG_TO_SHORT) shownModules.reverse()
        if(sort.value == Sort.ALPHABETICAL) shownModules.sortWith(compareBy { it.text.literalString })
    }

    override fun onRenderHud(drawer: HudDrawer, matrixStack: MatrixStack, delta: Float) {
        if(shownModules.isEmpty()) return

        for((i, info) in shownModules.withIndex()) {
            val xOffset = if(align.value == Alignment.RIGHT) longestWidth - info.width else 0f
            drawer.drawText(getFont(), info.text, matrixStack, getX() + 1f + xOffset, getY() + 1f + i * getLineHeight(), Color.WHITE, size = getSize())
        }
    }

    private fun getLineHeight() = getFont().getHeight(getSize()) + 2f

    private fun getStringWidth(text: Text) = getFont().getWidth(text, getSize())

    override fun getWidth() = longestWidth + 2f

    override fun getHeight() = getLineHeight() * shownModules.size

    private fun Module.getModuleListText(): Text {
        val info = getInfo() ?: return Text.literal(name)
        return Text.literal("$name ${TextColor.GRAY}[$info]")
    }

    private data class ModuleInfo(val text: Text, val width: Float)
}
