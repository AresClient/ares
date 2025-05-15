package org.aresclient.ares.impl.instrument.module.modules.hud

import org.aresclient.ares.api.render.Renderer
import org.aresclient.ares.api.render.TextColor
import org.aresclient.ares.api.util.Color
import org.aresclient.ares.impl.AresPlugin

object ToggleList: HudModule("Toggle List", "Shows whether specific modules are enabled or disabled.", position = Pair(0.5, 0.8), defaults = Defaults().setEnabled(true)) {

    enum class Sort { SHORT_TO_LONG, LONG_TO_SHORT, ALPHABETICAL }

    private val size = settings.addFloat("Size", 24f).setMin(5f).setMax(50f)
    private val delimiter = settings.addString("Delimiter", " : ")
    private val sort = settings.addEnum("Sort", Sort.SHORT_TO_LONG)

    private var shownModules = mutableMapOf<String, Boolean>()
    private var longest: String = ""

    private var delimeterOffset = 0f

    override fun onTick() {
        shownModules.clear()
        longest = ""

        for(module in AresPlugin.modules) {
            if(!module.externalCommons.showOnToggleList) continue

            shownModules[module.name] = module.isEnabled()
            if(module.name.length > longest.length) longest = module.name
        }

        delimeterOffset = getLeftWidth(longest)


        shownModules = when(sort.value) {
            Sort.SHORT_TO_LONG -> shownModules.toList().sortedWith(compareBy { it.first.length }).toMap().toMutableMap() // same problem as in ModuleList
            Sort.LONG_TO_SHORT -> shownModules.toList().sortedWith(compareBy { it.first.length }).reversed().toMap().toMutableMap()
            Sort.ALPHABETICAL  -> shownModules.toSortedMap()
        }
    }

    override fun onRenderHud(delta: Float, renderer: Renderer.State) {
        if(shownModules.isEmpty()) return

        var i = 0
        for(shown in shownModules) {
            val xOffset = delimeterOffset - getLeftWidth(shown.key)
            getFontRenderer().drawString(
                renderer.matrixStack, shown.asText(), size.value,
                getX() + xOffset, getY() + i * lineHeight,
                Color.WHITE
            )
            ++i
        }
    }

    private val lineHeight: Float get() = getFontRenderer().getCharHeight(size.value) + 2f

    private fun getStringWidth(string: String) = getFontRenderer().getStringWidth(string, size.value)

    private fun getLeftWidth(string: String) = getStringWidth(string) + (getStringWidth(delimiter.value) / 2)

    private fun Map.Entry<String, Boolean>.asText(): String = "${key}${delimiter.value}" + if(value) "${TextColor.GREEN}Enabled" else "${TextColor.RED}Disabled"

    override fun getWidth(): Float = getLeftWidth(longest) * 2 // Centres on the delimiter

    override fun getHeight(): Float = lineHeight * shownModules.size

}