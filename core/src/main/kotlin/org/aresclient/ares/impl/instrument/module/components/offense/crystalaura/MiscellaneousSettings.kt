package org.aresclient.ares.impl.instrument.module.components.offense.crystalaura

import org.aresclient.ares.api.instrument.Component.Settings
import org.aresclient.ares.impl.instrument.module.modules.offense.CrystalAura

object MiscellaneousSettings: Settings<CrystalAura>(CrystalAura, "Miscellaneous") {
    val order = settings
        .addEnum("Order", Order.BREAK_PLACE)
        .appendLines("The order in which each operation within a single instant will be attempted")
        .appendLines("Break Place - First attempt to break a crystal if one exists, then place a new crystal")
        .appendLines("Place Break - First attempt to place a crystal, then attempt break if one exists")
        .appendLines("Sequential - Only attempt to do one of break or place within a single instant")

    enum class Order {
        BREAK_PLACE,
        PLACE_BREAK,
        SEQUENTIAL
    }

    val remove_crystals_by = settings
        .addEnum("Remove Crystals By", RemovalType.SIMULATION)
        .appendLines("How dead crystals should be more quickly cleaned up")
        .appendLines("Simulation - Keep crystals on a list and pretend they aren't there")
        .appendLines("Setting Dead - Set crystals to dead so they are removed from the world")

    enum class RemovalType {
        SIMULATION,
        SETTING_DEAD
    }

    val remove_when = settings
        .addEnum("Remove When", RemoveWhen.EXPLOSION_PACKET_RECEIVED)
        .appendLines("Explosion Packet Received - Remove crystals as soon as the explosion packet is received from the corresponding location.")
        .appendLines("Attacked - Removes the crystal as soon as it is attacked by the player.")
        .appendLines("Normal - Don't attempt to remove crystals early.")

    enum class RemoveWhen {
        EXPLOSION_PACKET_RECEIVED,
        ATTACKED,
        NORMAL
    }
}