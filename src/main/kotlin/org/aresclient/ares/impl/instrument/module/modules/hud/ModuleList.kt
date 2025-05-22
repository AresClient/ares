package org.aresclient.ares.impl.instrument.module.modules.hud

import org.aresclient.ares.api.instruments.Module
import org.aresclient.ares.api.render.Renderer
import org.aresclient.ares.api.render.TextColor
import org.aresclient.ares.api.util.Color
import org.aresclient.ares.impl.AresPlugin

object ModuleList: HudModule("Module List", "Shows modules that are currently enabled in a list.", position = Pair(1.0, 1.0), defaults = Defaults().setEnabled(true)) {

    enum class Sort { SHORT_TO_LONG, LONG_TO_SHORT, ALPHABETICAL }

    enum class Alignment { LEFT, RIGHT }

    private val size = settings.addFloat("Size", 24f).setMin(5f).setMax(50f)
    private val sort = settings.addEnum("Sort", Sort.SHORT_TO_LONG)
    private val align = settings.addEnum("Align", Alignment.RIGHT)

    private var shownModules = mutableSetOf<String>()

    private var longestOffset = 0f

    override fun onTick() {
        shownModules.clear()

        var longest = ""

        for(module in AresPlugin.modules) {
            if(!module.externalCommons.showOnModuleList) continue

            shownModules.add(module.getModuleListText())
            if(module.name.length > longest.length) longest = module.name
        }

        longestOffset = getFontRenderer().getStringWidth(longest, size.value)

        shownModules = when(sort.value) {
            Sort.SHORT_TO_LONG -> shownModules.sortedBy { it.length }.toMutableSet() // For whatever reason `toSortedSet` causes some strings to disappear when comparing length
            Sort.LONG_TO_SHORT -> shownModules.sortedBy { it.length }.reversed().toMutableSet()
            Sort.ALPHABETICAL  -> shownModules.toSortedSet()
        }
    }

    override fun onRenderHud(delta: Float, renderer: Renderer.State) {
        if(shownModules.isEmpty()) return

        for((i, shown) in shownModules.withIndex()) {
            val xOffset = longestOffset - getFontRenderer().getStringWidth(shown, size.value)
            getFontRenderer().drawString(
                renderer.matrixStack, shown, size.value,
                getX() + if(align.value == Alignment.RIGHT) xOffset else 0f,
                getY() + i * lineHeight,
                Color.WHITE
            )
        }
    }

    private val lineHeight: Float get() = getFontRenderer().getCharHeight(size.value) + 2f

    override fun getWidth(): Float = longestOffset

    override fun getHeight(): Float = lineHeight * shownModules.size

    private fun Module.getModuleListText(): String {
        val info = getInfo() ?: return name
        return "$name ${TextColor.GRAY}[$info]"
    }
}
