package org.aresclient.ares.impl.instrument.modules.movement

import baritone.api.BaritoneAPI
import org.aresclient.ares.api.instruments.Module
import org.aresclient.ares.impl.util.BaritoneUtil

object Baritone: Module(Category.MOVEMENT, "Baritone", "Allows you to change the settings for baritone", defaults = Defaults().setAlwaysListening(true)) {
    private val allowSprint = settings.addBoolean("Allow Sprint", true).addListener(this::markDirty)
    private val allowBreak = settings.addBoolean("Allow Break", true).addListener(this::markDirty)
    private val allowParkour = settings.addBoolean("Allow Parkour", true).addListener(this::markDirty)
    private val allowParkourPlace = settings.addBoolean("Allow Parkour Place", true).addListener(this::markDirty)
    private val allowInventory = settings.addBoolean("Manage Inventory", false).addListener(this::markDirty)
    private val allowDownward = settings.addBoolean("Allow Downward", true).addListener(this::markDirty)
    private val freeLook = settings.addBoolean("Freelook", true).addListener(this::markDirty)
    private val renderGoal = settings.addBoolean("Render Goal", true).addListener(this::markDirty)
    private val enterPortal = settings.addBoolean("Avoid Portals", false).addListener(this::markDirty)

    private var dirty = true

    override fun onTick() {
        setEnabled(BaritoneUtil.isBaritonePresent()) // TODO: this probably isnt great to do every tick (need a way to override module toggling)
        if(!dirty) return

        BaritoneAPI.getSettings().allowSprint.value = allowSprint.value
        BaritoneAPI.getSettings().allowBreak.value = allowBreak.value
        BaritoneAPI.getSettings().allowParkour.value = allowParkour.value
        BaritoneAPI.getSettings().allowParkourPlace.value = allowParkourPlace.value
        BaritoneAPI.getSettings().allowInventory.value = allowInventory.value
        BaritoneAPI.getSettings().allowDownward.value = allowDownward.value
        BaritoneAPI.getSettings().freeLook.value = freeLook.value
        BaritoneAPI.getSettings().renderGoal.value = renderGoal.value
        BaritoneAPI.getSettings().enterPortal.value = enterPortal.value
    }

    private fun markDirty(value: Boolean) {
        dirty = true
    }
}
