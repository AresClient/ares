package org.aresclient.ares.impl.instrument.module.components.offense.crystalaura

import org.aresclient.ares.api.instrument.Component.Settings
import org.aresclient.ares.impl.instrument.module.modules.offense.CrystalAura

object MiscellaneousSettings: Settings<CrystalAura>(CrystalAura, "Miscellaneous") {
    val order = settings
        .addEnum("Order", Order.BREAK_PLACE)
        .setDescription("The order in which each operation within a single instant will be attempted")
        .setDescription("Break Place - First attempt to break a crystal if one exists, then place a new crystal")
        .setDescription("Place Break - First attempt to place a crystal, then attempt break if one exists")
        .setDescription("Sequential - Only attempt to do one of break or place within a single instant")

    enum class Order {
        BREAK_PLACE,
        PLACE_BREAK,
        SEQUENTIAL
    }

    val remove_crystals_by = settings
        .addEnum("Remove Crystals By", RemovalType.SIMULATION)
        .setDescription("How dead crystals should be more quickly cleaned up")
        .setDescription("Simulation - Keep crystals on a list and pretend they aren't there")
        .setDescription("Setting Dead - Set crystals to dead so they are removed from the world")

    enum class RemovalType {
        SIMULATION,
        SETTING_DEAD
    }

    val remove_when = settings
        .addEnum("Remove When", RemoveWhen.EXPLOSION_PACKET_RECEIVED)
        .setDescription("Explosion Packet Received - Remove crystals as soon as the explosion packet is received from the corresponding location.")
        .setDescription("Attacked - Removes the crystal as soon as it is attacked by the player.")
        .setDescription("Normal - Don't attempt to remove crystals early.")

    enum class RemoveWhen {
        EXPLOSION_PACKET_RECEIVED,
        ATTACKED,
        NORMAL
    }
}